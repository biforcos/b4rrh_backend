package com.b4rrhh.payroll.infrastructure.web.dto;

public record PayrollWarningResponse(
        String warningCode,
        String severityCode,
        String message,
        String detailsJson
) {
}
