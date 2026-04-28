package com.b4rrhh.payroll_engine.concept.application.usecase;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;

import java.util.List;

/**
 * Replaces the entire operand list of a concept identified by ({@code ruleSystemCode},
 * {@code conceptCode}). An empty {@code items} list clears every operand.
 *
 * <p>Each item declares the {@link OperandRole} (semantic position within the formula)
 * and the upstream concept that provides the value, identified by its concept code in
 * the same rule system.
 */
public record ReplaceConceptOperandsCommand(
        String ruleSystemCode,
        String conceptCode,
        List<Item> items
) {

    public record Item(OperandRole operandRole, String sourceObjectCode) {
    }
}
