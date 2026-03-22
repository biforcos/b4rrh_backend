package com.b4rrhh.employee.workcenter.infrastructure.web.dto;

import java.util.Map;

public record WorkCenterErrorResponse(
        String code,
        String message,
        Map<String, Object> details
) {
}