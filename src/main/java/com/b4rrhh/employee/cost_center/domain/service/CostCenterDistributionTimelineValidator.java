package com.b4rrhh.employee.cost_center.domain.service;

import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionPercentageExceededException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validates business rules that apply to a proposed distribution window before it is persisted.
 *
 * Rules enforced here:
 * - At least one item
 * - Each allocationPercentage > 0 (already enforced in CostCenterAllocation constructor)
 * - Sum of all allocationPercentages <= 100
 */
@Component
public class CostCenterDistributionTimelineValidator {

    private static final BigDecimal MAX_TOTAL = BigDecimal.valueOf(100);

    public void validateWindow(List<CostCenterAllocation> items) {
        if (items == null || items.isEmpty()) {
            throw new CostCenterDistributionInvalidException("a distribution window must have at least one item");
        }

        BigDecimal total = items.stream()
                .map(CostCenterAllocation::getAllocationPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(MAX_TOTAL) > 0) {
            throw new CostCenterDistributionPercentageExceededException(total);
        }
    }
}
