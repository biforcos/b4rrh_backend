package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
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
class ReplaceCostCenterDistributionFromDateServiceTest {

    private static final String RSC = "ESP";
    private static final String ETC = "INTERNAL";
    private static final String EN = "EMP001";
    private static final Long EMPLOYEE_ID = 10L;
    private static final LocalDate ORIGINAL_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2026, 4, 1);

    @Mock
    private CostCenterRepository costCenterRepository;
    @Mock
    private EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    @Mock
    private CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;

    private ReplaceCostCenterDistributionFromDateService service;

    @BeforeEach
    void setUp() {
        CostCenterCatalogValidator validator = new TestCatalogValidator();
        CostCenterDistributionTimelineValidator timelineValidator = new CostCenterDistributionTimelineValidator();
        service = new ReplaceCostCenterDistributionFromDateService(
                costCenterRepository,
                employeeCostCenterLookupPort,
                validator,
                costCenterPresenceConsistencyPort,
                timelineValidator
        );
    }

    // Test 9: replace-from-date closes previous active window and creates new one
    @Test
    void replacesActiveWindowAndCreatesNewOne() {
        givenEmployeeFound();
        CostCenterAllocation existingLine = new CostCenterAllocation(
                EMPLOYEE_ID, "CC_X", new BigDecimal("100"), ORIGINAL_START, null
        );
        when(costCenterRepository.findActiveAtDate(EMPLOYEE_ID, EFFECTIVE_DATE.minusDays(1)))
                .thenReturn(List.of(existingLine));
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(EMPLOYEE_ID, EFFECTIVE_DATE, null))
                .thenReturn(true);

        CostCenterDistributionWindow result = service.replaceFromDate(command(
                List.of(new CostCenterDistributionItem("CC_A", new BigDecimal("60")),
                        new CostCenterDistributionItem("CC_B", new BigDecimal("40")))
        ));

        assertNotNull(result);
        assertEquals(EFFECTIVE_DATE, result.getStartDate());
        assertNull(result.getEndDate());
        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("100"), result.getTotalAllocationPercentage());

        // Previous window must have been closed
        verify(costCenterRepository).closeAllForWindow(EMPLOYEE_ID, ORIGINAL_START, EFFECTIVE_DATE.minusDays(1));
        verify(costCenterRepository).saveAll(any());
    }

    // Test 16: replace-from-date does not reuse strong timeline logic (operates on DISTRIBUTED_TIMELINE windows)
    @Test
    void replaceUsesWindowStartDateNotIndividualLineStartDates() {
        givenEmployeeFound();
        // Two parallel lines forming a window with the same startDate
        CostCenterAllocation lineA = new CostCenterAllocation(
                EMPLOYEE_ID, "CC_X", new BigDecimal("60"), ORIGINAL_START, null
        );
        CostCenterAllocation lineB = new CostCenterAllocation(
                EMPLOYEE_ID, "CC_Y", new BigDecimal("40"), ORIGINAL_START, null
        );
        when(costCenterRepository.findActiveAtDate(EMPLOYEE_ID, EFFECTIVE_DATE.minusDays(1)))
                .thenReturn(List.of(lineA, lineB));
        when(costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(EMPLOYEE_ID, EFFECTIVE_DATE, null))
                .thenReturn(true);

        service.replaceFromDate(command(
                List.of(new CostCenterDistributionItem("CC_NEW", new BigDecimal("100")))
        ));

        // Both lines of the window (identified by ORIGINAL_START) must be closed together
        verify(costCenterRepository).closeAllForWindow(EMPLOYEE_ID, ORIGINAL_START, EFFECTIVE_DATE.minusDays(1));
    }

    @Test
    void rejectsWhenNoActiveDistributionBeforeEffectiveDate() {
        givenEmployeeFound();
        when(costCenterRepository.findActiveAtDate(EMPLOYEE_ID, EFFECTIVE_DATE.minusDays(1)))
                .thenReturn(List.of());

        assertThrows(CostCenterDistributionNotFoundException.class, () ->
                service.replaceFromDate(command(
                        List.of(new CostCenterDistributionItem("CC_A", new BigDecimal("100")))
                ))
        );
        verify(costCenterRepository, never()).saveAll(any());
    }

    @Test
    void rejectsWhenEmployeeNotFound() {
        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RSC, ETC, EN))
                .thenReturn(Optional.empty());

        assertThrows(CostCenterEmployeeNotFoundException.class, () ->
                service.replaceFromDate(command(
                        List.of(new CostCenterDistributionItem("CC_A", new BigDecimal("100")))
                ))
        );
    }

    @Test
    void rejectsWhenNoItemsProvided() {
        assertThrows(CostCenterDistributionInvalidException.class, () ->
                service.replaceFromDate(command(List.of()))
        );
    }

    // helpers

    private void givenEmployeeFound() {
        when(employeeCostCenterLookupPort.findByBusinessKeyForUpdate(RSC, ETC, EN))
                .thenReturn(Optional.of(new EmployeeCostCenterContext(EMPLOYEE_ID, RSC, ETC, EN)));
    }

    private ReplaceCostCenterDistributionFromDateCommand command(List<CostCenterDistributionItem> items) {
        return new ReplaceCostCenterDistributionFromDateCommand(RSC, ETC, EN, EFFECTIVE_DATE, items);
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
