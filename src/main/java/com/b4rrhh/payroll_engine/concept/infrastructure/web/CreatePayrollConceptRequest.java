package com.b4rrhh.payroll_engine.concept.infrastructure.web;

public record CreatePayrollConceptRequest(
        String conceptCode,
        String conceptMnemonic,
        String calculationType,
        String functionalNature,
        String resultCompositionMode,
        String executionScope,
        String payslipOrderCode
) {
}
