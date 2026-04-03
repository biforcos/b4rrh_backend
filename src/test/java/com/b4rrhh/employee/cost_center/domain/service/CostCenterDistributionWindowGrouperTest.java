package com.b4rrhh.employee.cost_center.domain.service;

import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionHistory;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CostCenterDistributionWindowGrouperTest {

    private static final Long EMPLOYEE_ID = 10L;

    private final CostCenterDistributionWindowGrouper grouper = new CostCenterDistributionWindowGrouper();

    @Test
    void returnsEmptyHistoryForEmptyInput() {
        CostCenterDistributionHistory history = grouper.group(List.of());
        assertTrue(history.getWindows().isEmpty());
    }

    @Test
    void groupsSingleLineAsSingleWindow() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        CostCenterAllocation a = allocation("CC_A", new BigDecimal("100"), start, null);

        CostCenterDistributionHistory history = grouper.group(List.of(a));

        assertEquals(1, history.getWindows().size());
        CostCenterDistributionWindow window = history.getWindows().get(0);
        assertEquals(start, window.getStartDate());
        assertNull(window.getEndDate());
        assertEquals(1, window.getItems().size());
    }

    @Test
    void groupsParallelLinesWithSameStartDateIntoOneWindow() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        CostCenterAllocation a = allocation("CC_A", new BigDecimal("50"), start, null);
        CostCenterAllocation b = allocation("CC_B", new BigDecimal("50"), start, null);

        CostCenterDistributionHistory history = grouper.group(List.of(a, b));

        assertEquals(1, history.getWindows().size());
        CostCenterDistributionWindow window = history.getWindows().get(0);
        assertEquals(2, window.getItems().size());
        assertEquals(new BigDecimal("100"), window.getTotalAllocationPercentage());
    }

    @Test
    void groupsTwoSequentialWindowsSeparately() {
        LocalDate start1 = LocalDate.of(2026, 1, 1);
        LocalDate end1 = LocalDate.of(2026, 3, 31);
        LocalDate start2 = LocalDate.of(2026, 4, 1);

        CostCenterAllocation window1a = allocation("CC_A", new BigDecimal("100"), start1, end1);
        CostCenterAllocation window2a = allocation("CC_B", new BigDecimal("60"), start2, null);
        CostCenterAllocation window2b = allocation("CC_C", new BigDecimal("40"), start2, null);

        CostCenterDistributionHistory history = grouper.group(List.of(window1a, window2a, window2b));

        assertEquals(2, history.getWindows().size());

        CostCenterDistributionWindow w1 = history.getWindows().get(0);
        assertEquals(start1, w1.getStartDate());
        assertEquals(end1, w1.getEndDate());
        assertEquals(1, w1.getItems().size());

        CostCenterDistributionWindow w2 = history.getWindows().get(1);
        assertEquals(start2, w2.getStartDate());
        assertNull(w2.getEndDate());
        assertEquals(2, w2.getItems().size());
    }

    @Test
    void historyFindsWindowActiveAtGivenDate() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        CostCenterAllocation a = allocation("CC_A", new BigDecimal("100"), start, null);
        CostCenterDistributionHistory history = grouper.group(List.of(a));

        assertTrue(history.findActiveAt(LocalDate.of(2026, 5, 15)).isPresent());
        assertTrue(history.findActiveAt(LocalDate.of(2025, 12, 31)).isEmpty());
    }

    private CostCenterAllocation allocation(String code, BigDecimal percentage, LocalDate start, LocalDate end) {
        return new CostCenterAllocation(EMPLOYEE_ID, code, percentage, start, end);
    }
}
