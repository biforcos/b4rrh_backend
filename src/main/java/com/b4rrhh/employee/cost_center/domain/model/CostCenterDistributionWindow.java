package com.b4rrhh.employee.cost_center.domain.model;

import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionStartDateMismatchException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A distribution window is the set of cost center lines for the same employee
 * that share the same startDate. It represents the complete allocation state
 * from that date onwards until the window is closed.
 *
 * This is the true business unit for cost center assignment — not the isolated row.
 */
public class CostCenterDistributionWindow {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<CostCenterAllocation> items;

    public CostCenterDistributionWindow(LocalDate startDate, LocalDate endDate, List<CostCenterAllocation> items) {
        if (startDate == null) {
            throw new CostCenterDistributionInvalidException("startDate is required");
        }
        if (items == null || items.isEmpty()) {
            throw new CostCenterDistributionInvalidException("a distribution window must have at least one item");
        }
        for (CostCenterAllocation item : items) {
            if (!startDate.equals(item.getStartDate())) {
                throw new CostCenterDistributionStartDateMismatchException(startDate, item.getStartDate());
            }
        }

        this.startDate = startDate;
        this.endDate = endDate;
        this.items = List.copyOf(items);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<CostCenterAllocation> getItems() {
        return items;
    }

    public BigDecimal getTotalAllocationPercentage() {
        return items.stream()
                .map(CostCenterAllocation::getAllocationPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isActive() {
        return items.stream().anyMatch(CostCenterAllocation::isActive);
    }
}
