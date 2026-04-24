package com.b4rrhh.payroll.application.service;

import java.time.LocalDate;

public record PayrollConceptExecutionContext(
        String ruleSystemCode,
        String agreementCode,
        String categoryCode,
        LocalDate referenceDate
) {
}