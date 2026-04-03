package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

import java.util.List;

public record CostCenterDistributionHistoryResponse(
        CostCenterEmployeeKeyResponse employee,
        List<CostCenterDistributionWindowResponse> windows
) {
}
