package com.b4rrhh.employee.cost_center.domain.model;

import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionStartDateMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CostCenterDistributionWindowTest {

    private static final LocalDate START = LocalDate.of(2026, 4, 1);

    @Test
    void computesTotalAllocationPercentage() {
        CostCenterAllocation a = allocation("CC_A", new BigDecimal("60"));
        CostCenterAllocation b = allocation("CC_B", new BigDecimal("40"));

        CostCenterDistributionWindow window = new CostCenterDistributionWindow(START, null, List.of(a, b));

        assertEquals(new BigDecimal("100"), window.getTotalAllocationPercentage());
    }

    @Test
    void isActiveWhenEndDateIsNull() {
        CostCenterAllocation a = allocation("CC_A", new BigDecimal("100"));

        CostCenterDistributionWindow window = new CostCenterDistributionWindow(START, null, List.of(a));

        assertTrue(window.isActive());
    }

    @Test
    void isNotActiveWhenEndDateIsSet() {
        CostCenterAllocation closed = new CostCenterAllocation(1L, "CC_A", new BigDecimal("100"), START, START.plusDays(30));

        CostCenterDistributionWindow window = new CostCenterDistributionWindow(START, START.plusDays(30), List.of(closed));

        assertFalse(window.isActive());
    }

    @Test
    void rejectsEmptyItems() {
        assertThrows(CostCenterDistributionInvalidException.class,
                () -> new CostCenterDistributionWindow(START, null, List.of()));
    }

    @Test
    void rejectsMismatchedStartDate() {
        CostCenterAllocation wrongStartDate = new CostCenterAllocation(
                1L, "CC_A", new BigDecimal("100"), START.plusDays(5), null
        );

        assertThrows(CostCenterDistributionStartDateMismatchException.class,
                () -> new CostCenterDistributionWindow(START, null, List.of(wrongStartDate)));
    }

    private CostCenterAllocation allocation(String code, BigDecimal percentage) {
        return new CostCenterAllocation(1L, code, percentage, START, null);
    }
}
