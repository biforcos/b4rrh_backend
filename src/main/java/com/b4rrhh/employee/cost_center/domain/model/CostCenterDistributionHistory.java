package com.b4rrhh.employee.cost_center.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The full temporal history of cost center distributions for an employee,
 * organized as an ordered list of distribution windows (oldest first).
 */
public class CostCenterDistributionHistory {

    private final List<CostCenterDistributionWindow> windows;

    public CostCenterDistributionHistory(List<CostCenterDistributionWindow> windows) {
        this.windows = windows == null ? List.of() : List.copyOf(windows);
    }

    public List<CostCenterDistributionWindow> getWindows() {
        return windows;
    }

    public Optional<CostCenterDistributionWindow> findActiveAt(LocalDate date) {
        return windows.stream()
                .filter(window -> isWindowActiveAt(window, date))
                .findFirst();
    }

    public Optional<CostCenterDistributionWindow> findByStartDate(LocalDate startDate) {
        return windows.stream()
                .filter(window -> startDate.equals(window.getStartDate()))
                .findFirst();
    }

    private boolean isWindowActiveAt(CostCenterDistributionWindow window, LocalDate date) {
        boolean startedOnOrBefore = !window.getStartDate().isAfter(date);
        boolean notYetEnded = window.getEndDate() == null || !window.getEndDate().isBefore(date);
        return startedOnOrBefore && notYetEnded;
    }
}
