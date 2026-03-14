package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterCatalogValidator;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterPresenceConsistencyValidator;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterPresenceCoverageGapException;
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
class CreateWorkCenterServiceTest {

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
    private CreateWorkCenterService service;

    @BeforeEach
    void setUp() {
        workCenterCatalogValidator = new TestWorkCenterCatalogValidator();
        service = new CreateWorkCenterService(
                workCenterRepository,
                employeeWorkCenterLookupPort,
                ruleSystemRepository,
                workCenterCatalogValidator,
                workCenterPresenceConsistencyValidator
        );
    }

    @Test
    void createsWorkCenterAndAssignsNextNumber() {
        CreateWorkCenterCommand command = new CreateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "madrid_hq",
                LocalDate.of(2026, 1, 10),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.of(2));
        when(workCenterRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null)).thenReturn(false);
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> {
            WorkCenter input = invocation.getArgument(0);
            return new WorkCenter(
                    99L,
                    input.getEmployeeId(),
                    input.getWorkCenterAssignmentNumber(),
                    input.getWorkCenterCode(),
                    input.getStartDate(),
                    input.getEndDate(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        });

        WorkCenter created = service.create(command);

        assertEquals(99L, created.getId());
        assertEquals(3, created.getWorkCenterAssignmentNumber());
        assertEquals("MADRID_HQ", created.getWorkCenterCode());

        ArgumentCaptor<WorkCenter> captor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterRepository).save(captor.capture());
        assertEquals(3, captor.getValue().getWorkCenterAssignmentNumber());

        InOrder inOrder = inOrder(employeeWorkCenterLookupPort, workCenterRepository, workCenterPresenceConsistencyValidator);
        inOrder.verify(employeeWorkCenterLookupPort)
                .findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
        inOrder.verify(workCenterRepository).existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null);
        inOrder.verify(workCenterPresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 1, 10),
                null,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );
    }

    @Test
    void allowsSameDayValidity() {
        CreateWorkCenterCommand command = new CreateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 10)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        when(workCenterRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 10)))
                .thenReturn(false);
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkCenter created = service.create(command);

        assertEquals(LocalDate.of(2026, 1, 10), created.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 10), created.getEndDate());
    }

    @Test
    void createsWorkCenterInsideValidPresencePeriod() {
        // Confirms that when presence validation passes (validator does not throw),
        // the work center assignment is persisted successfully.
        CreateWorkCenterCommand command = new CreateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "MAIN_OFFICE",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 30)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        when(workCenterRepository.existsOverlappingPeriod(
                        10L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(false);
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // workCenterPresenceConsistencyValidator.validatePeriodWithinPresence is a void mock that does not throw,
        // simulating a presence period that fully contains [2026-02-01, 2026-06-30].

        WorkCenter created = service.create(command);

        assertEquals(1, created.getWorkCenterAssignmentNumber());
        assertEquals("MAIN_OFFICE", created.getWorkCenterCode());
        assertEquals(LocalDate.of(2026, 2, 1), created.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 30), created.getEndDate());
        verify(workCenterPresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 30),
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );
        verify(workCenterRepository).save(any(WorkCenter.class));
    }

    @Test
    void rejectsOverlappingWorkCenterPeriod() {
        CreateWorkCenterCommand command = new CreateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.of(1));
        when(workCenterRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null)).thenReturn(true);

        assertThrows(WorkCenterOverlapException.class, () -> service.create(command));
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsInvalidCatalogValue() {
        CreateWorkCenterCommand command = new CreateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "bad",
                LocalDate.of(2026, 1, 10),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));

        assertThrows(WorkCenterCatalogValueInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsWhenOutsidePresenceHistory() {
        CreateWorkCenterCommand command = new CreateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        when(workCenterRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null)).thenReturn(false);

        doThrow(new WorkCenterOutsidePresencePeriodException(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 10),
                null
        )).when(workCenterPresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 1, 10),
                null,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );

        assertThrows(WorkCenterOutsidePresencePeriodException.class, () -> service.create(command));
    }

    @Test
    void rejectsWhenCoverageValidationFails() {
        CreateWorkCenterCommand command = new CreateWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        when(workCenterRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null)).thenReturn(false);
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        doThrow(new WorkCenterPresenceCoverageGapException(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .when(workCenterPresenceConsistencyValidator)
                .validatePresenceCoverageIfRequired(
                        any(Long.class),
                        org.mockito.ArgumentMatchers.<List<WorkCenter>>any(),
                        any(String.class),
                        any(String.class),
                        any(String.class)
                );

        assertThrows(WorkCenterPresenceCoverageGapException.class, () -> service.create(command));
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
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