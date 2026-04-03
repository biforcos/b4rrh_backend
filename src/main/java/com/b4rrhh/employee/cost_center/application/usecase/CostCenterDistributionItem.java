package com.b4rrhh.employee.cost_center.application.usecase;

import java.math.BigDecimal;

/** Represents a single line within a distribution window command. */
public record CostCenterDistributionItem(
        String costCenterCode,
        BigDecimal allocationPercentage
) {
}
