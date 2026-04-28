package com.b4rrhh.payroll_engine.concept.infrastructure.web;

/**
 * Wire representation of a single operand attached to a payroll concept.
 *
 * <p>Each item maps to a row in {@code payroll_concept_operand}: the role describes the
 * semantic position (QUANTITY, RATE, BASE, PERCENTAGE) and the {@code sourceObjectCode}
 * identifies the upstream concept that provides the value.
 */
public record ConceptOperandResponse(
        String operandRole,
        String sourceObjectCode
) {
}
