package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.port.WorkCenterPresenceConsistencyPort;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterDeleteForbiddenAtPresenceStartException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterNotFoundException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteWorkCenterServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private WorkCenterRepository workCenterRepository;
    @Mock
    private EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort;
    @Mock
    private WorkCenterPresenceConsistencyPort workCenterPresenceConsistencyPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;

    private DeleteWorkCenterService service;

    @BeforeEach
    void setUp() {
        service = new DeleteWorkCenterService(
                workCenterRepository,
                employeeWorkCenterLookupPort,
                workCenterPresenceConsistencyPort,
                ruleSystemRepository
        );
    }

    @Test
    void deletesWorkCenterAssignmentWhenNotAnchoredToPresenceStart() {
        WorkCenter existing = workCenter(1, 10L, LocalDate.of(2026, 1, 10));

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.of(existing));
        when(workCenterPresenceConsistencyPort.existsPresenceStartingAt(10L, LocalDate.of(2026, 1, 10)))
                .thenReturn(false);

        service.delete(new DeleteWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                1
        ));

        verify(workCenterRepository).delete(existing);
    }

    @Test
    void throwsNotFoundWhenAssignmentDoesNotExistForEmployeeBusinessKey() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.empty());

        assertThrows(
                WorkCenterNotFoundException.class,
                () -> service.delete(new DeleteWorkCenterCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        1
                ))
        );

        verify(workCenterRepository, never()).delete(org.mockito.ArgumentMatchers.any(WorkCenter.class));
    }

    @Test
    void throwsConflictWhenAssignmentStartDateMatchesPresenceStart() {
        WorkCenter existing = workCenter(1, 10L, LocalDate.of(2026, 1, 10));

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.of(existing));
        when(workCenterPresenceConsistencyPort.existsPresenceStartingAt(10L, LocalDate.of(2026, 1, 10)))
                .thenReturn(true);

        assertThrows(
                WorkCenterDeleteForbiddenAtPresenceStartException.class,
                () -> service.delete(new DeleteWorkCenterCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        1
                ))
        );

        verify(workCenterRepository, never()).delete(org.mockito.ArgumentMatchers.any(WorkCenter.class));
    }

    @Test
    void enforcesOwnershipByEmployeeBusinessKey() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 99))
                .thenReturn(Optional.empty());

        assertThrows(
                WorkCenterNotFoundException.class,
                () -> service.delete(new DeleteWorkCenterCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        99
                ))
        );

        verify(workCenterRepository).findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 99);
    }

    private WorkCenter workCenter(int assignmentNumber, Long employeeId, LocalDate startDate) {
        return new WorkCenter(
                (long) assignmentNumber,
                employeeId,
                assignmentNumber,
                "MADRID_HQ",
                startDate,
                null,
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
}