package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.util.Map;

public record HireEmployeeErrorResponse(
        String code,
        String message,
        Map<String, Object> details
) {
}
