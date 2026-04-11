package com.b4rrhh.payroll.infrastructure.web.dto;

import java.math.BigDecimal;

public record PayrollConceptRequest(
        Integer lineNumber,
        String conceptCode,
        String conceptLabel,
        BigDecimal amount,
        BigDecimal quantity,
        BigDecimal rate,
        String conceptNatureCode,
        String originPeriodCode,
        Integer displayOrder
) {
}