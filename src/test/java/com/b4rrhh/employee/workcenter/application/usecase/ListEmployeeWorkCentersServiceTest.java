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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmployeeWorkCentersServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private WorkCenterRepository workCenterRepository;
    @Mock
    private EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort;

    private ListEmployeeWorkCentersService service;

    @BeforeEach
    void setUp() {
        service = new ListEmployeeWorkCentersService(workCenterRepository, employeeWorkCenterLookupPort);
    }

    @Test
    void listsWorkCentersByEmployeeBusinessKey() {
        when(employeeWorkCenterLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(workCenterRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                workCenter(1, LocalDate.of(2026, 1, 10)),
                workCenter(2, LocalDate.of(2026, 2, 1))
        ));

        List<WorkCenter> result = service.listByEmployeeBusinessKey(" esp ", " internal ", " EMP001 ");

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getWorkCenterAssignmentNumber());
        assertEquals(2, result.get(1).getWorkCenterAssignmentNumber());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeWorkCenterLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                WorkCenterEmployeeNotFoundException.class,
                () -> service.listByEmployeeBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER)
        );
    }

    private EmployeeWorkCenterContext employeeContext(Long employeeId) {
        return new EmployeeWorkCenterContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private WorkCenter workCenter(int assignmentNumber, LocalDate startDate) {
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
}