package com.b4rrhh.employee.cost_center.infrastructure.rest.dto;

import java.math.BigDecimal;

public record UpdateCostCenterRequest(
        BigDecimal allocationPercentage
) {
}
