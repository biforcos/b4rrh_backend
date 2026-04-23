package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.AgreementAnnualHoursLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.WorkingTimePresenceConsistencyValidator;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNumberConflictException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOutsidePresencePeriodException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOverlapException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateWorkingTimeServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";
    private static final String AGREEMENT_CODE = "99002405011982";
    private static final BigDecimal ANNUAL_HOURS = new BigDecimal("1736.00");
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2026, 1, 10);

    @Mock
    private WorkingTimeRepository workingTimeRepository;
    @Mock
    private EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;
    @Mock
    private EmployeeAgreementContextLookupPort employeeAgreementContextLookupPort;
    @Mock
    private AgreementAnnualHoursLookupPort agreementAnnualHoursLookupPort;
    @Mock
    private WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator;
    @Mock
    private WorkingTimeDerivationPolicy workingTimeDerivationPolicy;

    private CreateWorkingTimeService service;

    @BeforeEach
    void setUp() {
        service = new CreateWorkingTimeService(
                workingTimeRepository,
                employeeWorkingTimeLookupPort,
                employeeAgreementContextLookupPort,
                agreementAnnualHoursLookupPort,
                workingTimePresenceConsistencyValidator,
                workingTimeDerivationPolicy
        );
    }

    @Test
    void createsWorkingTimeAndCalculatesDerivedHours() {
        CreateWorkingTimeCommand command = new CreateWorkingTimeCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                EFFECTIVE_DATE,
                new BigDecimal("50")
        );
        WorkingTimeDerivedHours derivedHours = new WorkingTimeDerivedHours(
                new BigDecimal("16.69"),
                new BigDecimal("3.34"),
                new BigDecimal("72.33")
        );

        when(employeeWorkingTimeLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        stubAgreementResolution(10L, EFFECTIVE_DATE);
        when(workingTimeRepository.findMaxWorkingTimeNumberByEmployeeId(10L)).thenReturn(Optional.of(2));
        when(workingTimeRepository.existsOverlappingPeriod(10L, EFFECTIVE_DATE, null)).thenReturn(false);
        stubDerivedHours(new BigDecimal("50"), derivedHours);
        when(workingTimeRepository.save(any(WorkingTime.class))).thenAnswer(invocation -> {
            WorkingTime input = invocation.getArgument(0);
            return WorkingTime.rehydrate(
                    99L,
                    input.getEmployeeId(),
                    input.getWorkingTimeNumber(),
                    input.getStartDate(),
                    input.getEndDate(),
                    input.getWorkingTimePercentage(),
                    new WorkingTimeDerivedHours(
                            input.getWeeklyHours(),
                            input.getDailyHours(),
                            input.getMonthlyHours()
                    ),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        });

        WorkingTime created = service.create(command);

        assertEquals(99L, created.getId());
        assertEquals(3, created.getWorkingTimeNumber());
        assertEquals(0, created.getWorkingTimePercentage().compareTo(new BigDecimal("50")));
        assertEquals(new BigDecimal("16.69"), created.getWeeklyHours());
        assertEquals(new BigDecimal("3.34"), created.getDailyHours());
        assertEquals(new BigDecimal("72.33"), created.getMonthlyHours());

        ArgumentCaptor<WorkingTime> captor = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository).save(captor.capture());
        verify(workingTimeDerivationPolicy, atLeastOnce())
                .derive(
                        argThat(pct -> pct != null && pct.compareTo(new BigDecimal("50")) == 0),
                        argThat(hrs -> hrs != null && hrs.compareTo(ANNUAL_HOURS) == 0)
                );
        assertEquals(3, captor.getValue().getWorkingTimeNumber());
        assertEquals(new BigDecimal("16.69"), captor.getValue().getWeeklyHours());
        assertEquals(new BigDecimal("3.34"), captor.getValue().getDailyHours());
        assertEquals(new BigDecimal("72.33"), captor.getValue().getMonthlyHours());
    }

    @Test
    void rejectsOverlappingWorkingTimePeriod() {
        CreateWorkingTimeCommand command = new CreateWorkingTimeCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                EFFECTIVE_DATE,
                new BigDecimal("80")
        );

        when(employeeWorkingTimeLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        stubAgreementResolution(10L, EFFECTIVE_DATE);
        when(workingTimeRepository.findMaxWorkingTimeNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        stubDerivedHours(new BigDecimal("80"), new WorkingTimeDerivedHours(
                new BigDecimal("26.71"), new BigDecimal("5.34"), new BigDecimal("115.73")));
        when(workingTimeRepository.existsOverlappingPeriod(10L, EFFECTIVE_DATE, null)).thenReturn(true);

        assertThrows(WorkingTimeOverlapException.class, () -> service.create(command));
        verify(workingTimeRepository, never()).save(any(WorkingTime.class));
    }

    @Test
    void rejectsWhenOutsidePresenceHistory() {
        CreateWorkingTimeCommand command = new CreateWorkingTimeCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                EFFECTIVE_DATE,
                new BigDecimal("80")
        );

        when(employeeWorkingTimeLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        stubAgreementResolution(10L, EFFECTIVE_DATE);
        when(workingTimeRepository.findMaxWorkingTimeNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        stubDerivedHours(new BigDecimal("80"), new WorkingTimeDerivedHours(
                new BigDecimal("26.71"), new BigDecimal("5.34"), new BigDecimal("115.73")));
        when(workingTimeRepository.existsOverlappingPeriod(10L, EFFECTIVE_DATE, null)).thenReturn(false);

        doThrow(new WorkingTimeOutsidePresencePeriodException(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                EFFECTIVE_DATE,
                null
        )).when(workingTimePresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                EFFECTIVE_DATE,
                null,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );

        assertThrows(WorkingTimeOutsidePresencePeriodException.class, () -> service.create(command));
    }

    @Test
    void translatesFunctionalNumberUniquenessConflict() {
        CreateWorkingTimeCommand command = new CreateWorkingTimeCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                EFFECTIVE_DATE,
                new BigDecimal("80")
        );

        when(employeeWorkingTimeLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        stubAgreementResolution(10L, EFFECTIVE_DATE);
        when(workingTimeRepository.findMaxWorkingTimeNumberByEmployeeId(10L)).thenReturn(Optional.of(1));
        stubDerivedHours(new BigDecimal("80"), new WorkingTimeDerivedHours(
                new BigDecimal("26.71"), new BigDecimal("5.34"), new BigDecimal("115.73")));
        when(workingTimeRepository.existsOverlappingPeriod(10L, EFFECTIVE_DATE, null)).thenReturn(false);
        when(workingTimeRepository.save(any(WorkingTime.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate working_time_number"));

        assertThrows(WorkingTimeNumberConflictException.class, () -> service.create(command));
    }

    private EmployeeWorkingTimeContext employeeContext(Long employeeId) {
        return new EmployeeWorkingTimeContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private void stubAgreementResolution(Long employeeId, LocalDate effectiveDate) {
        when(employeeAgreementContextLookupPort.resolveContext(employeeId, effectiveDate))
                .thenReturn(new EmployeeAgreementContext(RULE_SYSTEM_CODE, AGREEMENT_CODE));
        when(agreementAnnualHoursLookupPort.resolveAnnualHours(RULE_SYSTEM_CODE, AGREEMENT_CODE))
                .thenReturn(ANNUAL_HOURS);
    }

    private void stubDerivedHours(BigDecimal percentage, WorkingTimeDerivedHours derivedHours) {
        when(workingTimeDerivationPolicy.derive(
                argThat(pct -> pct != null && pct.compareTo(percentage) == 0),
                argThat(hrs -> hrs != null && hrs.compareTo(ANNUAL_HOURS) == 0)
        )).thenReturn(derivedHours);
    }
}