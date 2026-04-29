package com.b4rrhh.payroll_engine.concept.application.usecase;

public record UpdateConceptSummaryCommand(
        String ruleSystemCode,
        String conceptCode,
        String summary
) {}
