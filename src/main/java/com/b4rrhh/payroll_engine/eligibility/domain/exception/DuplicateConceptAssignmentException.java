package com.b4rrhh.payroll_engine.eligibility.domain.exception;

/**
 * Thrown when eligibility resolution finds two or more concept assignments for the same
 * {@code conceptCode} that share the same (highest) priority in a given context.
 *
 * <p>This is a data-integrity problem: the caller must fix the assignment data so that
 * no ambiguous priority collision exists for the target concept and context.
 */
public class DuplicateConceptAssignmentException extends RuntimeException {

    public DuplicateConceptAssignmentException(
            String ruleSystemCode,
            String conceptCode,
            int priority
    ) {
        super("Ambiguous concept assignment: multiple assignments for conceptCode='" + conceptCode
                + "' share the same highest priority=" + priority
                + " in ruleSystem='" + ruleSystemCode + "'."
                + " Resolve the conflict by adjusting priority values in the assignment data.");
    }
}
