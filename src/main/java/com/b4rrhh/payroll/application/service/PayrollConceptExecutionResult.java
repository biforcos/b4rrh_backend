package com.b4rrhh.payroll.application.service;

import java.math.BigDecimal;

public record PayrollConceptExecutionResult(
        String conceptCode,
        BigDecimal amount,
        BigDecimal quantity,
        BigDecimal rate
) {
}