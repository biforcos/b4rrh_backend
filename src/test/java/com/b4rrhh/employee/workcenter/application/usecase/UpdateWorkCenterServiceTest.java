package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterCatalogValidator;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterPresenceConsistencyValidator;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateWorkCenterServiceTest {

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

    private WorkCenterCatalogValidator workCenterCatalogValidator;
    private UpdateWorkCenterService service;

    @BeforeEach
    void setUp() {
        workCenterCatalogValidator = new TestWorkCenterCatalogValidator();
        service = new UpdateWorkCenterService(
                workCenterRepository,
                employeeWorkCenterLookupPort,
                ruleSystemRepository,
                workCenterCatalogValidator,
                workCenterPresenceConsistencyValidator
        );
    }

    @Test
    void updatesWorkCenterForEmployeeAssignment() {
        UpdateWorkCenterCommand command = new UpdateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                2,
                "barcelona_hq",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 20)
        );

        WorkCenter existing = workCenter(
                40L,
                10L,
                2,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 2))
                .thenReturn(Optional.of(existing));
        when(workCenterRepository.existsOverlappingPeriodExcludingAssignment(
                10L,
                2,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 20)
        )).thenReturn(false);
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkCenter updated = service.update(command);

        assertEquals(40L, updated.getId());
        assertEquals(2, updated.getWorkCenterAssignmentNumber());
        assertEquals("BARCELONA_HQ", updated.getWorkCenterCode());
        assertEquals(LocalDate.of(2026, 2, 1), updated.getStartDate());
        assertEquals(LocalDate.of(2026, 2, 20), updated.getEndDate());

        ArgumentCaptor<WorkCenter> captor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getWorkCenterAssignmentNumber());
        assertEquals(40L, captor.getValue().getId());

        InOrder inOrder = inOrder(workCenterRepository, workCenterPresenceConsistencyValidator);
        inOrder.verify(workCenterRepository).existsOverlappingPeriodExcludingAssignment(
                10L,
                2,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 20)
        );
        inOrder.verify(workCenterPresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 20),
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );
    }

    @Test
    void rejectsUpdateWhenAssignmentDoesNotBelongToEmployee() {
        UpdateWorkCenterCommand command = new UpdateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                2,
                "BARCELONA_HQ",
                LocalDate.of(2026, 2, 1),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 2))
                .thenReturn(Optional.empty());

        assertThrows(WorkCenterNotFoundException.class, () -> service.update(command));
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsOverlappingPeriodAgainstOtherAssignments() {
        UpdateWorkCenterCommand command = new UpdateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                2,
                "BARCELONA_HQ",
                LocalDate.of(2026, 2, 1),
                null
        );

        WorkCenter existing = workCenter(
                40L,
                10L,
                2,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 2))
                .thenReturn(Optional.of(existing));
        when(workCenterRepository.existsOverlappingPeriodExcludingAssignment(
                10L,
                2,
                LocalDate.of(2026, 2, 1),
                null
        )).thenReturn(true);

        assertThrows(WorkCenterOverlapException.class, () -> service.update(command));
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsUpdateOutsidePresenceHistory() {
        UpdateWorkCenterCommand command = new UpdateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                2,
                "BARCELONA_HQ",
                LocalDate.of(2026, 2, 1),
                null
        );

        WorkCenter existing = workCenter(
                40L,
                10L,
                2,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 2))
                .thenReturn(Optional.of(existing));
        when(workCenterRepository.existsOverlappingPeriodExcludingAssignment(
                10L,
                2,
                LocalDate.of(2026, 2, 1),
                null
        )).thenReturn(false);

        doThrow(new WorkCenterOutsidePresencePeriodException(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 2, 1),
                null
        )).when(workCenterPresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 2, 1),
                null,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );

        assertThrows(WorkCenterOutsidePresencePeriodException.class, () -> service.update(command));
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsInvalidCatalogValue() {
        UpdateWorkCenterCommand command = new UpdateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                2,
                "bad",
                LocalDate.of(2026, 2, 1),
                null
        );

        WorkCenter existing = workCenter(
                40L,
                10L,
                2,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 2))
                .thenReturn(Optional.of(existing));

        assertThrows(WorkCenterCatalogValueInvalidException.class, () -> service.update(command));
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
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

    private EmployeeWorkCenterContext employeeContext(Long employeeId) {
        return new EmployeeWorkCenterContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
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
        public void validateWorkCenterCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate) {
            if ("BAD".equals(workCenterCode)) {
                throw new WorkCenterCatalogValueInvalidException("workCenterCode", workCenterCode);
            }
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new WorkCenterCatalogValueInvalidException(fieldName, String.valueOf(value));
            }

            return value.trim().toUpperCase();
        }
    }
}
