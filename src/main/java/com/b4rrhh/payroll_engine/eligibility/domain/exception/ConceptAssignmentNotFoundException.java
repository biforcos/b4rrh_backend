package com.b4rrhh.payroll_engine.eligibility.domain.exception;

/**
 * Thrown when a concept assignment lookup by ({@code ruleSystemCode}, {@code assignmentCode})
 * does not match any persisted row. Surfaces as 404 in the management endpoints.
 */
public class ConceptAssignmentNotFoundException extends RuntimeException {

    public ConceptAssignmentNotFoundException(String ruleSystemCode, String assignmentCode) {
        super("ConceptAssignment not found: ruleSystemCode=" + ruleSystemCode
                + ", assignmentCode=" + assignmentCode);
    }
}
