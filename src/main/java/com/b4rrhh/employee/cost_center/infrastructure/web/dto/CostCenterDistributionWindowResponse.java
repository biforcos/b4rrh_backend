package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CostCenterDistributionWindowResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalAllocationPercentage,
        List<CostCenterDistributionItemResponse> items
) {
}
