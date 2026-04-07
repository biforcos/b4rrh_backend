package com.b4rrhh.employee.working_time.infrastructure.web.dto;

import java.util.Map;

public record WorkingTimeErrorResponse(
        String code,
        String message,
        Map<String, Object> details
) {
}