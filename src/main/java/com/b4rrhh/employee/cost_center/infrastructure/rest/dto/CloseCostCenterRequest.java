package com.b4rrhh.employee.cost_center.infrastructure.rest.dto;

import java.time.LocalDate;

public record CloseCostCenterRequest(
        LocalDate endDate
) {
}
