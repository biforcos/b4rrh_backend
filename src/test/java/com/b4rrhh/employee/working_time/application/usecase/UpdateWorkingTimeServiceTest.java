package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.AgreementAnnualHoursLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.WorkingTimePresenceConsistencyValidator;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeAlreadyClosedException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateWorkingTimeServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";
    private static final String AGREEMENT_CODE = "99002405011982";
    private static final BigDecimal ANNUAL_HOURS = new BigDecimal("1736.00");

    private static final WorkingTimeDerivedHours DERIVED_HOURS = new WorkingTimeDerivedHours(
            new BigDecimal("20.00"),
            new BigDecimal("4.00"),
            new BigDecimal("86.80")
    );

    @Mock private WorkingTimeRepository workingTimeRepository;
    @Mock private EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;
    @Mock private EmployeeAgreementContextLookupPort employeeAgreementContextLookupPort;
    @Mock private AgreementAnnualHoursLookupPort agreementAnnualHoursLookupPort;
    @Mock private WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator;
    @Mock private WorkingTimeDerivationPolicy workingTimeDerivationPolicy;

    private UpdateWorkingTimeService service;

    @BeforeEach
    void setUp() {
        service = new UpdateWorkingTimeService(
                workingTimeRepository,
                employeeWorkingTimeLookupPort,
                employeeAgreementContextLookupPort,
                agreementAnnualHoursLookupPort,
                workingTimePresenceConsistencyValidator,
                workingTimeDerivationPolicy
        );
    }

    @Test
    void updatesWhenValid() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        BigDecimal newPercentage = new BigDecimal("50");

        WorkingTime existing = activeWorkingTime(1, startDate, new BigDecimal("40"));

        whenEmployeeExists();
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(10L, 1))
                .thenReturn(Optional.of(existing));
        when(workingTimeRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(existing));
        stubAgreementResolution(10L, startDate);
        when(workingTimeDerivationPolicy.derive(newPercentage, ANNUAL_HOURS)).thenReturn(DERIVED_HOURS);
        when(workingTimeRepository.existsOverlappingPeriodExcluding(10L, startDate, null, 1))
                .thenReturn(false);
        when(workingTimeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WorkingTime updated = service.update(new UpdateWorkingTimeCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, 1, startDate, newPercentage
        ));

        assertThat(updated.getWorkingTimePercentage()).isEqualByComparingTo(newPercentage);
        assertThat(updated.getStartDate()).isEqualTo(startDate);

        ArgumentCaptor<WorkingTime> captor = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getWorkingTimePercentage()).isEqualByComparingTo(newPercentage);
    }

    @Test
    void rejectsUpdateWhenRecordIsClosed() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        WorkingTime closed = WorkingTime.rehydrate(
                1L, 10L, 1, startDate, LocalDate.of(2026, 1, 31),
                new BigDecimal("40"), DERIVED_HOURS, LocalDateTime.now(), LocalDateTime.now()
        );

        whenEmployeeExists();
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(10L, 1))
                .thenReturn(Optional.of(closed));

        assertThrows(WorkingTimeAlreadyClosedException.class, () -> service.update(
                new UpdateWorkingTimeCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
                        1, startDate, new BigDecimal("50"))
        ));
        verify(workingTimeRepository, never()).save(any());
    }

    @Test
    void whenStartDateDiffers_predecessorEndDateIsCascaded() {
        LocalDate predecessorStart = LocalDate.of(2024, 1, 1);
        LocalDate predecessorEnd   = LocalDate.of(2024, 12, 31);
        LocalDate currentStart     = LocalDate.of(2025, 1, 1);
        LocalDate newStart         = LocalDate.of(2025, 2, 1);
        BigDecimal newPercentage   = new BigDecimal("60");

        WorkingTime predecessor = WorkingTime.rehydrate(
                99L, 10L, 1, predecessorStart, predecessorEnd,
                new BigDecimal("40"), DERIVED_HOURS, LocalDateTime.now(), LocalDateTime.now()
        );
        WorkingTime current = activeWorkingTime(2, currentStart, new BigDecimal("40"));

        whenEmployeeExists();
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(10L, 2))
                .thenReturn(Optional.of(current));
        when(workingTimeRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(predecessor, current));
        stubAgreementResolution(10L, newStart);
        when(workingTimeDerivationPolicy.derive(newPercentage, ANNUAL_HOURS)).thenReturn(DERIVED_HOURS);
        when(workingTimeRepository.existsOverlappingPeriodExcluding(any(), any(), any(), any()))
                .thenReturn(false);
        when(workingTimeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(new UpdateWorkingTimeCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, 2, newStart, newPercentage
        ));

        ArgumentCaptor<WorkingTime> captor = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository, times(2)).save(captor.capture());

        WorkingTime savedPredecessor = captor.getAllValues().stream()
                .filter(wt -> wt.getWorkingTimeNumber().equals(1))
                .findFirst().orElseThrow();
        WorkingTime savedCurrent = captor.getAllValues().stream()
                .filter(wt -> wt.getWorkingTimeNumber().equals(2))
                .findFirst().orElseThrow();

        assertThat(savedPredecessor.getEndDate()).isEqualTo(newStart.minusDays(1));
        assertThat(savedCurrent.getStartDate()).isEqualTo(newStart);
    }

    @Test
    void whenStartDateDiffers_andNoPredecessor_onlySavesUpdated() {
        LocalDate currentStart = LocalDate.of(2025, 1, 1);
        LocalDate newStart     = LocalDate.of(2025, 2, 1);
        BigDecimal newPercentage = new BigDecimal("50");

        WorkingTime current = activeWorkingTime(1, currentStart, new BigDecimal("40"));

        whenEmployeeExists();
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(10L, 1))
                .thenReturn(Optional.of(current));
        when(workingTimeRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(current));
        stubAgreementResolution(10L, newStart);
        when(workingTimeDerivationPolicy.derive(newPercentage, ANNUAL_HOURS)).thenReturn(DERIVED_HOURS);
        when(workingTimeRepository.existsOverlappingPeriodExcluding(any(), any(), any(), any()))
                .thenReturn(false);
        when(workingTimeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(new UpdateWorkingTimeCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, 1, newStart, newPercentage
        ));

        ArgumentCaptor<WorkingTime> captor = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStartDate()).isEqualTo(newStart);
    }

    private void whenEmployeeExists() {
        when(employeeWorkingTimeLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER
        )).thenReturn(Optional.of(new EmployeeWorkingTimeContext(
                10L, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER
        )));
    }

    private void stubAgreementResolution(Long employeeId, LocalDate startDate) {
        when(employeeAgreementContextLookupPort.resolveContext(employeeId, startDate))
                .thenReturn(new EmployeeAgreementContext(RULE_SYSTEM_CODE, AGREEMENT_CODE));
        when(agreementAnnualHoursLookupPort.resolveAnnualHours(RULE_SYSTEM_CODE, AGREEMENT_CODE))
                .thenReturn(ANNUAL_HOURS);
    }

    private static WorkingTime activeWorkingTime(int number, LocalDate startDate, BigDecimal percentage) {
        return WorkingTime.rehydrate(
                (long) number,
                10L,
                number,
                startDate,
                null,
                percentage,
                DERIVED_HOURS,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
