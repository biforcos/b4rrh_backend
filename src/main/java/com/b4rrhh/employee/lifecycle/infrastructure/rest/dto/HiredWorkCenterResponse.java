package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record HiredWorkCenterResponse(
        Integer workCenterAssignmentNumber,
        String workCenterCode,
        LocalDate startDate
) {
}
