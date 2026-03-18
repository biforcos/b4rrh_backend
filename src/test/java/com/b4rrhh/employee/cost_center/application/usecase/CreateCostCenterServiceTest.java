package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.CreateCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOverlapException;
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
class CreateCostCenterServiceTest {

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
    private CreateCostCenterService service;

    @BeforeEach
    void setUp() {
        costCenterCatalogValidator = new TestCostCenterCatalogValidator();
        service = new CreateCostCenterService(
                costCenterRepository,
                employeeCostCenterLookupPort,
                costCenterCatalogValidator,
                costCenterPresenceConsistencyPort
        );
    }

    @Test
    void rejectsWhenEmployeeDoesNotExist() {
        CreateCostCenterCommand command = command("CC01", new BigDecimal("50"), LocalDate.of(2026, 1, 1), null);

        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(CostCenterEmployeeNotFoundException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidCatalogValue() {
        CreateCostCenterCommand command = command("BAD", new BigDecimal("50"), LocalDate.of(2026, 1, 1), null);

        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));

        assertThrows(CostCenterCatalogValueInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsWhenOutsidePresencePeriod() {
        CreateCostCenterCommand command = command("CC01", new BigDecimal("60"), LocalDate.of(2026, 1, 1), null);

        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(costCenterRepository.existsOverlappingPeriodByCostCenterCode(
                10L,
                "CC01",
                LocalDate.of(2026, 1, 1),
                null
        )).thenReturn(false);
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null
        )).thenReturn(false);

        assertThrows(CostCenterOutsidePresencePeriodException.class, () -> service.create(command));
        verify(costCenterRepository, never()).save(any(CostCenterAllocation.class));
    }

    @Test
    void rejectsOverlappingPeriodForSameCostCenter() {
        CreateCostCenterCommand command = command("CC01", new BigDecimal("80"), LocalDate.of(2026, 1, 10), null);

        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(costCenterRepository.existsOverlappingPeriodByCostCenterCode(
                10L,
                "CC01",
                LocalDate.of(2026, 1, 10),
                null
        )).thenReturn(true);

        assertThrows(CostCenterOverlapException.class, () -> service.create(command));
        verify(costCenterRepository, never()).save(any(CostCenterAllocation.class));
    }

    @Test
    void allowsOverlappingWhenCostCenterIsDifferent() {
        CreateCostCenterCommand command = command("CC02", new BigDecimal("100"), LocalDate.of(2026, 1, 15), null);

        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(costCenterRepository.existsOverlappingPeriodByCostCenterCode(
                10L,
                "CC02",
                LocalDate.of(2026, 1, 15),
                null
        )).thenReturn(false);
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                10L,
                LocalDate.of(2026, 1, 15),
                null
        )).thenReturn(true);

        CostCenterAllocation created = service.create(command);

        assertEquals("CC02", created.getCostCenterCode());
        assertEquals(new BigDecimal("100"), created.getAllocationPercentage());
        verify(costCenterRepository).save(any(CostCenterAllocation.class));
    }

    private CreateCostCenterCommand command(
            String costCenterCode,
            BigDecimal allocationPercentage,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new CreateCostCenterCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                costCenterCode,
                allocationPercentage,
                startDate,
                endDate
        );
    }

    private EmployeeCostCenterContext employeeContext(Long employeeId) {
        return new EmployeeCostCenterContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private static final class TestCostCenterCatalogValidator extends CostCenterCatalogValidator {

        private TestCostCenterCatalogValidator() {
            super(null);
        }

        @Override
        public void validateCostCenterCode(String ruleSystemCode, String costCenterCode, LocalDate referenceDate) {
            if ("BAD".equals(costCenterCode)) {
                throw new CostCenterCatalogValueInvalidException("costCenterCode", costCenterCode);
            }
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new CostCenterCatalogValueInvalidException(fieldName, String.valueOf(value));
            }

            return value.trim().toUpperCase();
        }
    }
}
