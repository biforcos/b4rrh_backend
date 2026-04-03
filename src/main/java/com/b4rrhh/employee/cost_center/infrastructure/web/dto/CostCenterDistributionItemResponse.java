package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

import java.math.BigDecimal;

public record CostCenterDistributionItemResponse(
        String costCenterCode,
        String costCenterName,
        BigDecimal allocationPercentage
) {
}
