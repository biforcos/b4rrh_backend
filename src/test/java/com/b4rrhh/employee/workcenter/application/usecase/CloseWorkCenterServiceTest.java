package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterPresenceConsistencyValidator;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterAlreadyClosedException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseWorkCenterServiceTest {

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

    private CloseWorkCenterService service;

    @BeforeEach
    void setUp() {
        service = new CloseWorkCenterService(
                workCenterRepository,
                employeeWorkCenterLookupPort,
                ruleSystemRepository,
                workCenterPresenceConsistencyValidator
        );
    }

    @Test
    void closesWorkCenterAssignment() {
        WorkCenter existing = activeWorkCenter(1, LocalDate.of(2026, 1, 10));

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.of(existing));
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkCenter closed = service.close(new CloseWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                1,
                LocalDate.of(2026, 1, 20)
        ));

        assertEquals(LocalDate.of(2026, 1, 20), closed.getEndDate());

        ArgumentCaptor<WorkCenter> captor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterRepository).save(captor.capture());
        assertEquals(LocalDate.of(2026, 1, 20), captor.getValue().getEndDate());
    }

    @Test
    void allowsSameDayClose() {
        WorkCenter existing = activeWorkCenter(1, LocalDate.of(2026, 1, 10));

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.of(existing));
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));
        when(workCenterRepository.save(any(WorkCenter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkCenter closed = service.close(new CloseWorkCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                1,
                LocalDate.of(2026, 1, 10)
        ));

        assertEquals(LocalDate.of(2026, 1, 10), closed.getEndDate());
    }

    @Test
    void rejectsCloseWhenAssignmentDoesNotExist() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1)).thenReturn(Optional.empty());

        assertThrows(
                WorkCenterNotFoundException.class,
                () -> service.close(new CloseWorkCenterCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        1,
                        LocalDate.of(2026, 1, 20)
                ))
        );
    }

    @Test
    void rejectsCloseWhenAlreadyClosed() {
        WorkCenter existing = new WorkCenter(
                11L,
                10L,
                1,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 15),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.of(existing));

        assertThrows(
                WorkCenterAlreadyClosedException.class,
                () -> service.close(new CloseWorkCenterCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        1,
                        LocalDate.of(2026, 1, 20)
                ))
        );
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void rejectsCloseWhenResultingPeriodIsOutsidePresenceHistory() {
        WorkCenter existing = activeWorkCenter(1, LocalDate.of(2026, 1, 10));

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeWorkCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.of(existing));

        doThrow(new WorkCenterOutsidePresencePeriodException(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20)
        )).when(workCenterPresenceConsistencyValidator).validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20),
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        );

        assertThrows(
                WorkCenterOutsidePresencePeriodException.class,
                () -> service.close(new CloseWorkCenterCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        1,
                        LocalDate.of(2026, 1, 20)
                ))
        );
    }

    private WorkCenter activeWorkCenter(int assignmentNumber, LocalDate startDate) {
        return new WorkCenter(
                (long) assignmentNumber,
                10L,
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