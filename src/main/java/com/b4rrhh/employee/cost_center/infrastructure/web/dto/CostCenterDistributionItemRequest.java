package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

import java.math.BigDecimal;

public record CostCenterDistributionItemRequest(
        String costCenterCode,
        BigDecimal allocationPercentage
) {
}
