package com.b4rrhh.employee.cost_center.domain.service;

import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionHistory;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups a flat list of CostCenterAllocation rows (from persistence) into
 * distribution windows, preserving temporal order.
 *
 * All rows that share the same startDate belong to the same window.
 * The window's endDate is the common endDate of its rows (or null if open).
 */
@Component
public class CostCenterDistributionWindowGrouper {

    public CostCenterDistributionHistory group(List<CostCenterAllocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return new CostCenterDistributionHistory(List.of());
        }

        Map<LocalDate, List<CostCenterAllocation>> byStartDate = new LinkedHashMap<>();

        List<CostCenterAllocation> sorted = allocations.stream()
                .sorted(Comparator.comparing(CostCenterAllocation::getStartDate))
                .toList();

        for (CostCenterAllocation allocation : sorted) {
            byStartDate.computeIfAbsent(allocation.getStartDate(), k -> new ArrayList<>()).add(allocation);
        }

        List<CostCenterDistributionWindow> windows = byStartDate.entrySet().stream()
                .map(entry -> {
                    LocalDate startDate = entry.getKey();
                    List<CostCenterAllocation> windowItems = entry.getValue();
                    LocalDate endDate = resolveWindowEndDate(windowItems);
                    return new CostCenterDistributionWindow(startDate, endDate, windowItems);
                })
                .toList();

        return new CostCenterDistributionHistory(windows);
    }

    /**
     * Resolves the window's endDate from its items.
     * If all items are closed and share the same endDate, that is the window's endDate.
     * If any item is open (endDate == null), the window is considered open.
     */
    private LocalDate resolveWindowEndDate(List<CostCenterAllocation> items) {
        boolean anyOpen = items.stream().anyMatch(a -> a.getEndDate() == null);
        if (anyOpen) {
            return null;
        }

        return items.stream()
                .map(CostCenterAllocation::getEndDate)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
