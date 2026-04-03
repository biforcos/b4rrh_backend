package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

public record CostCenterCurrentDistributionResponse(
        CostCenterEmployeeKeyResponse employee,
        CostCenterDistributionWindowResponse currentDistribution
) {
}
