package com.b4rrhh.payroll_engine.execution.domain.exception;

/**
 * Thrown when a required PoC concept cannot be found in the {@code payroll_engine} repository.
 *
 * <p>This exception signals a data setup problem: the executing rule system is missing
 * one of the concept definitions that the PoC executor requires to build the dependency
 * graph and execution plan.
 */
public class MissingPocConceptException extends RuntimeException {

    public MissingPocConceptException(String ruleSystemCode, String conceptCode) {
        super("Required PoC concept not found in repository: " + ruleSystemCode + "/" + conceptCode);
    }
}
