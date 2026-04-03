package com.b4rrhh.employee.cost_center.infrastructure.web.dto;

import java.time.LocalDate;

public record CloseCostCenterDistributionRequest(
        LocalDate endDate
) {
}
