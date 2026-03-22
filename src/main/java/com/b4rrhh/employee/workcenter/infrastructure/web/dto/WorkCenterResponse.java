package com.b4rrhh.employee.workcenter.infrastructure.web.dto;

import java.time.LocalDate;

public record WorkCenterResponse(
        Integer workCenterAssignmentNumber,
        String workCenterCode,
        String workCenterName,
        LocalDate startDate,
        LocalDate endDate
) {
}