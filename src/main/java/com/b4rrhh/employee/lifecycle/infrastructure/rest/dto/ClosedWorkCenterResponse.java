package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record ClosedWorkCenterResponse(
        Integer workCenterAssignmentNumber,
        String workCenterCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
