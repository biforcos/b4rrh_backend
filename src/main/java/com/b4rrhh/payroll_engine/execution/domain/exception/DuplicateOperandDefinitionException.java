package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;

/**
 * Thrown when more than one operand definition with the same role is found for a concept.
 * This normally indicates a data integrity violation that was not caught by the unique constraint.
 */
public class DuplicateOperandDefinitionException extends RuntimeException {

    public DuplicateOperandDefinitionException(String ruleSystemCode, String conceptCode, OperandRole role) {
        super("Duplicate operand definition for concept '" + ruleSystemCode + "/" + conceptCode
                + "' with role " + role + ". Only one operand per role is allowed.");
    }
}
