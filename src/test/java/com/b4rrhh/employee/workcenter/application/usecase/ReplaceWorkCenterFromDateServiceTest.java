package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterCatalogValidator;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterPresenceConsistencyValidator;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCompanyMismatchException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterEmployeeNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterPresenceCoverageGapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.EmployeeActiveCompanyLookupPort;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterCompanyLookupPort;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
import com.b4rrhh.employee.workcenter.domain.service.WorkCenterEmployeeCompanyDomainService;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplaceWorkCenterFromDateServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private WorkCenterRepository workCenterRepository;
    @Mock
    private EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;
    @Mock
    private WorkCenterPresenceConsistencyValidator workCenterPresenceConsistencyValidator;
        @Mock
        private WorkCenterCompanyLookupPort workCenterCompanyLookupPort;
        @Mock
        private EmployeeActiveCompanyLookupPort employeeActiveCompanyLookupPort;

    private WorkCenterCatalogValidator workCenterCatalogValidator;
        private WorkCenterEmployeeCompanyDomainService workCenterEmployeeCompanyDomainService;
    private ReplaceWorkCenterFromDateService service;

    @BeforeEach
    void setUp() {
        workCenterCatalogValidator = new TestWorkCenterCatalogValidator();
        workCenterEmployeeCompanyDomainService = new WorkCenterEmployeeCompanyDomainService(
                employeeActiveCompanyLookupPort,
                workCenterCompanyLookupPort
        );
        service = new ReplaceWorkCenterFromDateService(
                workCenterRepository,
                employeeWorkCenterLookupPort,
                ruleSystemRepository,
                workCenterCatalogValidator,
                workCenterPresenceConsistencyValidator,
                workCenterEmployeeCompanyDomainService
        );
    }

    @Test
    void replaceWhenActiveAssignmentExistsClosesCurrentAndCreatesNewOne() {
        WorkCenter existing = workCenter(
                41L,
                10L,
                2,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenRuleSystemAndEmployeeExist();
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.of(2));
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));
        when(workCenterCompanyLookupPort.findCompanyCode(RULE_SYSTEM_CODE, "BARCELONA_HQ", LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkCenter replaced = service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "barcelona_hq"));

        assertEquals(3, replaced.getWorkCenterAssignmentNumber());
        assertEquals("BARCELONA_HQ", replaced.getWorkCenterCode());
        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(null, replaced.getEndDate());

        ArgumentCaptor<WorkCenter> saveCaptor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterRepository, times(2)).save(saveCaptor.capture());

        List<WorkCenter> savedValues = saveCaptor.getAllValues();
        assertEquals(LocalDate.of(2026, 2, 28), savedValues.get(0).getEndDate());
        assertEquals(2, savedValues.get(0).getWorkCenterAssignmentNumber());
        assertEquals(LocalDate.of(2026, 3, 1), savedValues.get(1).getStartDate());
        assertEquals(3, savedValues.get(1).getWorkCenterAssignmentNumber());
    }

    @Test
    void replaceWhenNoActiveAssignmentCreatesNewOneDirectlyIfValid() {
        whenRuleSystemAndEmployeeExist();
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));
        when(workCenterCompanyLookupPort.findCompanyCode(RULE_SYSTEM_CODE, "SEVILLA_HQ", LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkCenter replaced = service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "sevilla_hq"));

        assertEquals(1, replaced.getWorkCenterAssignmentNumber());
        assertEquals("SEVILLA_HQ", replaced.getWorkCenterCode());
        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(null, replaced.getEndDate());

        ArgumentCaptor<WorkCenter> saveCaptor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterRepository).save(saveCaptor.capture());
        assertEquals(LocalDate.of(2026, 3, 1), saveCaptor.getValue().getStartDate());
        assertEquals(null, saveCaptor.getValue().getEndDate());
    }

    @Test
    void rejectsWhenEmployeeDoesNotExist() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.empty());

        assertThrows(
                WorkCenterEmployeeNotFoundException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "BARCELONA_HQ"))
        );

        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsWhenWorkCenterCodeIsInvalid() {
        whenRuleSystemAndEmployeeExist();

        assertThrows(
                WorkCenterCatalogValueInvalidException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "bad"))
        );

        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsWhenProjectedTimelineOverlapsFutureAssignment() {
        WorkCenter futureOpen = workCenter(
                52L,
                10L,
                2,
                "BILBAO_HQ",
                LocalDate.of(2026, 4, 1),
                null
        );

        whenRuleSystemAndEmployeeExist();
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(futureOpen));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.of(2));
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));
        when(workCenterCompanyLookupPort.findCompanyCode(RULE_SYSTEM_CODE, "BARCELONA_HQ", LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));

        assertThrows(
                WorkCenterOverlapException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "BARCELONA_HQ"))
        );

        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsWhenReplacementFallsOutsidePresence() {
        WorkCenter existing = workCenter(
                41L,
                10L,
                2,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenRuleSystemAndEmployeeExist();
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.of(2));

        doThrow(new WorkCenterOutsidePresencePeriodException(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 3, 1),
                null
        )).when(workCenterPresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 3, 1),
                null,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );

        assertThrows(
                WorkCenterOutsidePresencePeriodException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "BARCELONA_HQ"))
        );

        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsWhenReplacementBreaksPresenceCoverage() {
        WorkCenter existing = workCenter(
                41L,
                10L,
                2,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenRuleSystemAndEmployeeExist();
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.of(2));
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));
        when(workCenterCompanyLookupPort.findCompanyCode(RULE_SYSTEM_CODE, "BARCELONA_HQ", LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));

        doThrow(new WorkCenterPresenceCoverageGapException(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).when(workCenterPresenceConsistencyValidator).validatePresenceCoverageIfRequired(
                any(Long.class),
                org.mockito.ArgumentMatchers.<List<WorkCenter>>any(),
                any(String.class),
                any(String.class),
                any(String.class)
        );

        assertThrows(
                WorkCenterPresenceCoverageGapException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "BARCELONA_HQ"))
        );

        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsWhenReplacementWorkCenterBelongsToDifferentCompany() {
        whenRuleSystemAndEmployeeExist();
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("COMP"));
        when(workCenterCompanyLookupPort.findCompanyCode(RULE_SYSTEM_CODE, "BARCELONA_HQ", LocalDate.of(2026, 3, 1)))
                .thenReturn(Optional.of("OTHER"));

        assertThrows(
                WorkCenterCompanyMismatchException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "BARCELONA_HQ"))
        );

        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    private void whenRuleSystemAndEmployeeExist() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.of(new EmployeeWorkCenterContext(
                10L,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )));
    }

    private ReplaceWorkCenterFromDateCommand command(LocalDate effectiveDate, String workCenterCode) {
        return new ReplaceWorkCenterFromDateCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                effectiveDate,
                workCenterCode
        );
    }

    private WorkCenter workCenter(
            Long id,
            Long employeeId,
            Integer assignmentNumber,
            String workCenterCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new WorkCenter(
                id,
                employeeId,
                assignmentNumber,
                workCenterCode,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private RuleSystem ruleSystem(String code) {
        return new RuleSystem(
                1L,
                code,
                "Spain",
                "ESP",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
    private static final class TestWorkCenterCatalogValidator extends WorkCenterCatalogValidator {

        private TestWorkCenterCatalogValidator() {
            super(null);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new WorkCenterCatalogValueInvalidException(fieldName, String.valueOf(value));
            }

            return value.trim().toUpperCase();
        }

        @Override
        public void validateWorkCenterCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate) {
            if ("BAD".equals(workCenterCode)) {
                throw new WorkCenterCatalogValueInvalidException("workCenterCode", workCenterCode);
            }
        }
    }
}