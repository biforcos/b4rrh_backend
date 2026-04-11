package com.b4rrhh.payroll.domain.model;

public record PayrollWarning(
        Long id,
        Long payrollId,
        String warningCode,
        String severityCode,
        String message,
        String detailsJson
) {
}