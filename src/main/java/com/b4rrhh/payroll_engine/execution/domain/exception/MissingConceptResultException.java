package com.b4rrhh.payroll_engine.execution.domain.exception;

/**
 * Thrown when a concept required as a dependency has not yet been evaluated
 * in the current segment execution state.
 *
 * <p>This indicates either a missing entry in the execution plan or a plan
 * that is not in topological order.
 */
public class MissingConceptResultException extends RuntimeException {

    public MissingConceptResultException(String message) {
        super(message);
    }
}
