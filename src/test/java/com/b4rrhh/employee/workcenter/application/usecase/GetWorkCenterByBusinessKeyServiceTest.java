package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterEmployeeNotFoundException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkCenterByBusinessKeyServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private WorkCenterRepository workCenterRepository;
    @Mock
    private EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort;

    private GetWorkCenterByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new GetWorkCenterByBusinessKeyService(workCenterRepository, employeeWorkCenterLookupPort);
    }

    @Test
    void getsWorkCenterByBusinessKey() {
        when(employeeWorkCenterLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.of(workCenter(1)));

        Optional<WorkCenter> result = service.getByBusinessKey(" esp ", " internal ", " EMP001 ", 1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getWorkCenterAssignmentNumber());
        assertEquals("MADRID_HQ", result.get().getWorkCenterCode());
    }

    @Test
    void returnsEmptyWhenAssignmentDoesNotExist() {
        when(employeeWorkCenterLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdAndWorkCenterAssignmentNumber(10L, 1))
                .thenReturn(Optional.empty());

        Optional<WorkCenter> result = service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeWorkCenterLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                WorkCenterEmployeeNotFoundException.class,
                () -> service.getByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, 1)
        );
    }

    private EmployeeWorkCenterContext employeeContext(Long employeeId) {
        return new EmployeeWorkCenterContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private WorkCenter workCenter(int assignmentNumber) {
        return new WorkCenter(
                (long) assignmentNumber,
                10L,
                assignmentNumber,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}