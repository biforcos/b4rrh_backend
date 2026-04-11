package com.b4rrhh.payroll.domain.model;

import java.time.LocalDateTime;

public record CalculationRunMessage(
        Long id,
        Long runId,
        String messageCode,
        String severityCode,
        String message,
        String detailsJson,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber,
        LocalDateTime createdAt
) {
}