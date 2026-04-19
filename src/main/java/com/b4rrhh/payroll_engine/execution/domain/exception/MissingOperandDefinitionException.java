package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;

/**
 * Thrown when no operand definition is found for a required role on a RATE_BY_QUANTITY concept.
 */
public class MissingOperandDefinitionException extends RuntimeException {

    public MissingOperandDefinitionException(String ruleSystemCode, String conceptCode, OperandRole role) {
        super("No operand definition found for concept '" + ruleSystemCode + "/" + conceptCode
                + "' with role " + role + ".");
    }
}
