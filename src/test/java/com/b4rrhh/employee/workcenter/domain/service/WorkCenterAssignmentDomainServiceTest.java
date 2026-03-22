package com.b4rrhh.employee.workcenter.domain.service;

import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import com.b4rrhh.employee.workcenter.domain.port.EmployeePresencePort;
import com.b4rrhh.employee.workcenter.domain.port.RuleEntityValidationPort;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkCenterAssignmentDomainServiceTest {

    @Mock
    private WorkCenterAssignmentRepository workCenterAssignmentRepository;
    @Mock
    private EmployeePresencePort employeePresencePort;
    @Mock
    private RuleEntityValidationPort ruleEntityValidationPort;

    private WorkCenterAssignmentDomainService service;

    @BeforeEach
    void setUp() {
        service = new WorkCenterAssignmentDomainService(
                workCenterAssignmentRepository,
                employeePresencePort,
                ruleEntityValidationPort
        );
    }

    @Test
    void createRejectsOverlap() {
        WorkCenterAssignment assignment = assignment(2, "MADRID_HQ", LocalDate.of(2026, 1, 10), null);

        when(ruleEntityValidationPort.existsActiveWorkCenterCode("ESP", "MADRID_HQ", LocalDate.of(2026, 1, 10)))
                .thenReturn(true);
        when(employeePresencePort.existsPresenceContainingPeriod(10L, LocalDate.of(2026, 1, 10), null))
                .thenReturn(true);
        when(workCenterAssignmentRepository.existsOverlappingPeriod(10L, LocalDate.of(2026, 1, 10), null))
                .thenReturn(true);

        assertThrows(WorkCenterOverlapException.class, () -> service.validateCreate("ESP", "INTERNAL", "EMP001", assignment));
    }

    @Test
    void createRejectsOutsidePresence() {
        WorkCenterAssignment assignment = assignment(2, "MADRID_HQ", LocalDate.of(2026, 1, 10), null);

        when(ruleEntityValidationPort.existsActiveWorkCenterCode("ESP", "MADRID_HQ", LocalDate.of(2026, 1, 10)))
                .thenReturn(true);
        when(employeePresencePort.existsPresenceContainingPeriod(10L, LocalDate.of(2026, 1, 10), null))
                .thenReturn(false);

        assertThrows(WorkCenterOutsidePresencePeriodException.class, () -> service.validateCreate("ESP", "INTERNAL", "EMP001", assignment));
    }

    @Test
    void createRejectsCatalogNotFound() {
        WorkCenterAssignment assignment = assignment(2, "MADRID_HQ", LocalDate.of(2026, 1, 10), null);

        when(ruleEntityValidationPort.existsActiveWorkCenterCode("ESP", "MADRID_HQ", LocalDate.of(2026, 1, 10)))
                .thenReturn(false);

        assertThrows(WorkCenterCatalogValueInvalidException.class, () -> service.validateCreate("ESP", "INTERNAL", "EMP001", assignment));
    }

    @Test
    void updateRejectsOverlapExcludingSameAssignment() {
        WorkCenterAssignment assignment = assignment(2, "BARCELONA_HQ", LocalDate.of(2026, 2, 1), null);

        when(ruleEntityValidationPort.existsActiveWorkCenterCode("ESP", "BARCELONA_HQ", LocalDate.of(2026, 2, 1)))
                .thenReturn(true);
        when(employeePresencePort.existsPresenceContainingPeriod(10L, LocalDate.of(2026, 2, 1), null))
                .thenReturn(true);
        when(workCenterAssignmentRepository.existsOverlappingPeriodExcludingAssignment(10L, 2, LocalDate.of(2026, 2, 1), null))
                .thenReturn(true);

        assertThrows(WorkCenterOverlapException.class, () -> service.validateUpdate("ESP", "INTERNAL", "EMP001", assignment));
    }

    private WorkCenterAssignment assignment(Integer number, String code, LocalDate startDate, LocalDate endDate) {
        return new WorkCenterAssignment(
                20L,
                10L,
                number,
                code,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
