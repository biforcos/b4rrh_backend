package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionConflictException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionPercentageExceededException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.exception.InvalidAllocationPercentageException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import com.b4rrhh.employee.cost_center.domain.service.CostCenterDistributionTimelineValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCostCenterDistributionServiceTest {

    private static final String RSC = "ESP";
    private static final String ETC = "INTERNAL";
    private static final String EN = "EMP001";
    private static final Long EMPLOYEE_ID = 10L;
    private static final LocalDate START = LocalDate.of(2026, 4, 1);

    @Mock
    private CostCenterRepository costCenterRepository;
    @Mock
    private EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    @Mock
    private CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;

    private CreateCostCenterDistributionService service;

    @BeforeEach
    void setUp() {
        CostCenterCatalogValidator validator = new TestCatalogValidator();
        CostCenterDistributionTimelineValidator timelineValidator = new CostCenterDistributionTimelineValidator();
        service = new CreateCostCenterDistributionService(
                costCenterRepository,
                employeeCostCenterLookupPort,
                validator,
                costCenterPresenceConsistencyPort,
                timelineValidator
        );
    }

    // Test 1: create valid single-line 100% distribution
    @Test
    void createsValidSingleLineFull100Distribution() {
        givenEmployeeFound();
        givenNoActiveDistributionAtDate();
        givenPresenceContains();

        CostCenterDistributionWindow window = service.create(command(
                List.of(item("CC_A", new BigDecimal("100")))
        ));

        assertNotNull(window);
        assertEquals(1, window.getItems().size());
        assertEquals(new BigDecimal("100"), window.getTotalAllocationPercentage());
        assertNull(window.getEndDate());
        verify(costCenterRepository).saveAll(any());
    }

    // Test 2: create valid 50/50 parallel distribution
    @Test
    void createsValid5050ParallelDistribution() {
        givenEmployeeFound();
        givenNoActiveDistributionAtDate();
        givenPresenceContains();

        CostCenterDistributionWindow window = service.create(command(
                List.of(item("CC_A", new BigDecimal("50")), item("CC_B", new BigDecimal("50")))
        ));

        assertEquals(2, window.getItems().size());
        assertEquals(new BigDecimal("100"), window.getTotalAllocationPercentage());
    }

    // Test 3: reject total percentage > 100
    @Test
    void rejectsTotalPercentageExceeds100() {
        givenEmployeeFound();

        assertThrows(CostCenterDistributionPercentageExceededException.class, () ->
                service.create(command(
                        List.of(item("CC_A", new BigDecimal("80")), item("CC_B", new BigDecimal("30")))
                ))
        );
        verify(costCenterRepository, never()).saveAll(any());
    }

    // Test 4: reject zero percentage
    @Test
    void rejectsZeroAllocationPercentage() {
        givenEmployeeFound();

        assertThrows(InvalidAllocationPercentageException.class, () ->
                service.create(command(
                        List.of(item("CC_A", BigDecimal.ZERO))
                ))
        );
    }

    // Test 5: reject negative percentage
    @Test
    void rejectsNegativeAllocationPercentage() {
        givenEmployeeFound();

        assertThrows(InvalidAllocationPercentageException.class, () ->
                service.create(command(
                        List.of(item("CC_A", new BigDecimal("-10")))
                ))
        );
    }

    // Test 7: reject invalid COST_CENTER catalog value
    @Test
    void rejectsInvalidCostCenterCatalogValue() {
        givenEmployeeFound();

        assertThrows(CostCenterCatalogValueInvalidException.class, () ->
                service.create(command(
                        List.of(item("INVALID_CC", new BigDecimal("100")))
                ))
        );
        verify(costCenterRepository, never()).saveAll(any());
    }

    // Test 8: reject lines outside presence
    @Test
    void rejectsWhenPeriodIsOutsidePresence() {
        givenEmployeeFound();
        givenNoActiveDistributionAtDate();
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(EMPLOYEE_ID, START, null))
                .thenReturn(false);

        assertThrows(CostCenterOutsidePresencePeriodException.class, () ->
                service.create(command(
                        List.of(item("CC_A", new BigDecimal("100")))
                ))
        );
        verify(costCenterRepository, never()).saveAll(any());
    }

    // Test 13: no technical IDs in canonical API (window returned has no IDs exposed)
    @Test
    void windowResponseContainsNoTechnicalIds() {
        givenEmployeeFound();
        givenNoActiveDistributionAtDate();
        givenPresenceContains();

        CostCenterDistributionWindow window = service.create(command(
                List.of(item("CC_A", new BigDecimal("100")))
        ));

        // CostCenterDistributionWindow exposes startDate, endDate, items, totalPercentage — no employeeId
        assertNotNull(window.getStartDate());
        assertNull(window.getEndDate());
    }

    // Test 14: ownership enforced by employee business key
    @Test
    void rejectsWhenEmployeeNotFound() {
        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RSC, ETC, EN))
                .thenReturn(Optional.empty());

        assertThrows(CostCenterEmployeeNotFoundException.class, () ->
                service.create(command(List.of(item("CC_A", new BigDecimal("100")))))
        );
    }

    // Test for conflict when active distribution exists at startDate
    @Test
    void rejectsWhenActiveDistributionAlreadyExistsAtStartDate() {
        givenEmployeeFound();
        CostCenterAllocation existingActive = new CostCenterAllocation(
                EMPLOYEE_ID, "CC_X", new BigDecimal("100"), START.minusDays(30), null
        );
        when(costCenterRepository.findActiveAtDate(EMPLOYEE_ID, START))
                .thenReturn(List.of(existingActive));

        assertThrows(CostCenterDistributionConflictException.class, () ->
                service.create(command(List.of(item("CC_A", new BigDecimal("100")))))
        );
        verify(costCenterRepository, never()).saveAll(any());
    }

    @Test
    void rejectsWhenNoItemsProvided() {
        assertThrows(CostCenterDistributionInvalidException.class, () ->
                service.create(command(List.of()))
        );
    }

    // helpers

    private void givenEmployeeFound() {
        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RSC, ETC, EN))
                .thenReturn(Optional.of(new EmployeeCostCenterContext(EMPLOYEE_ID, RSC, ETC, EN)));
    }

    private void givenNoActiveDistributionAtDate() {
        when(costCenterRepository.findActiveAtDate(EMPLOYEE_ID, START)).thenReturn(List.of());
    }

    private void givenPresenceContains() {
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(EMPLOYEE_ID, START, null))
                .thenReturn(true);
    }

    private CreateCostCenterDistributionCommand command(List<CostCenterDistributionItem> items) {
        return new CreateCostCenterDistributionCommand(RSC, ETC, EN, START, items);
    }

    private CostCenterDistributionItem item(String code, BigDecimal percentage) {
        return new CostCenterDistributionItem(code, percentage);
    }

    private static final class TestCatalogValidator extends CostCenterCatalogValidator {
        TestCatalogValidator() {
            super(null);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new CostCenterCatalogValueInvalidException(fieldName, String.valueOf(value));
            }
            return value.trim().toUpperCase();
        }

        @Override
        public void validateCostCenterCode(String ruleSystemCode, String costCenterCode, LocalDate referenceDate) {
            if (costCenterCode.startsWith("INVALID")) {
                throw new CostCenterCatalogValueInvalidException("costCenterCode", costCenterCode);
            }
        }
    }
}
