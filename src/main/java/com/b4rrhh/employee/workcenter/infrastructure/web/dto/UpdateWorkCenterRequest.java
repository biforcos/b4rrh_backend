package com.b4rrhh.employee.workcenter.infrastructure.web.dto;

import java.time.LocalDate;

public record UpdateWorkCenterRequest(
        String workCenterCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
