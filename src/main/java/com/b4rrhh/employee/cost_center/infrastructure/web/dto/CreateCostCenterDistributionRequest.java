package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateCostCenterDistributionRequest(
        LocalDate startDate,
        List<CostCenterDistributionItemRequest> items
) {
}
