package com.b4rrhh.employee.cost_center.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CostCenterResponse(
        String costCenterCode,
        BigDecimal allocationPercentage,
        LocalDate startDate,
        LocalDate endDate
) {
}
