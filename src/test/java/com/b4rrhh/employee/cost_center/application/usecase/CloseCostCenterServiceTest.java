package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.CloseCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterAlreadyClosedException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseCostCenterServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private CostCenterRepository costCenterRepository;
    @Mock
    private EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    @Mock
    private CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;

    private CostCenterCatalogValidator costCenterCatalogValidator;
    private CloseCostCenterService service;

    @BeforeEach
    void setUp() {
        costCenterCatalogValidator = new TestCostCenterCatalogValidator();
        service = new CloseCostCenterService(
                costCenterRepository,
                employeeCostCenterLookupPort,
                costCenterCatalogValidator,
                costCenterPresenceConsistencyPort
        );
    }

    @Test
    void closesActiveAllocation() {
        CloseCostCenterCommand command = new CloseCostCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "CC01",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        CostCenterAllocation existing = new CostCenterAllocation(
                10L,
                "CC01",
                new BigDecimal("50"),
                LocalDate.of(2026, 1, 1),
                null
        );

        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(costCenterRepository.findByEmployeeIdAndCostCenterCodeAndStartDate(
                10L,
                "CC01",
                LocalDate.of(2026, 1, 1)
        )).thenReturn(Optional.of(existing));
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        )).thenReturn(true);

        CostCenterAllocation closed = service.close(command);

        assertEquals(LocalDate.of(2026, 1, 31), closed.getEndDate());
        verify(costCenterRepository).update(any(CostCenterAllocation.class));
    }

    @Test
    void rejectsCloseWhenAlreadyClosed() {
        CloseCostCenterCommand command = new CloseCostCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "CC01",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
        );

        CostCenterAllocation closed = new CostCenterAllocation(
                10L,
                "CC01",
                new BigDecimal("50"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(costCenterRepository.findByEmployeeIdAndCostCenterCodeAndStartDate(
                10L,
                "CC01",
                LocalDate.of(2026, 1, 1)
        )).thenReturn(Optional.of(closed));

        assertThrows(CostCenterAlreadyClosedException.class, () -> service.close(command));
        verify(costCenterPresenceConsistencyPort, never()).existsPresenceContainingPeriod(any(), any(), any());
        verify(costCenterRepository, never()).update(any(CostCenterAllocation.class));
    }

    private EmployeeCostCenterContext employeeContext(Long employeeId) {
        return new EmployeeCostCenterContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private static final class TestCostCenterCatalogValidator extends CostCenterCatalogValidator {

        private TestCostCenterCatalogValidator() {
            super(null);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            return value == null ? null : value.trim().toUpperCase();
        }
    }
}
