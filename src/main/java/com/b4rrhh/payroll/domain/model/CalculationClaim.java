package com.b4rrhh.payroll.domain.model;

import java.time.LocalDateTime;

public record CalculationClaim(
        Long id,
        Long runId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber,
        LocalDateTime claimedAt,
        String claimedBy
) {
}