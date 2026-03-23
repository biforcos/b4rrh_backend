package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record RehiredWorkCenterResponse(
        Integer workCenterAssignmentNumber,
        String workCenterCode,
        LocalDate startDate
) {
}
