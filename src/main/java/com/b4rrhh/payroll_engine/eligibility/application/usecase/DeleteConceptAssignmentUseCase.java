package com.b4rrhh.payroll_engine.eligibility.application.usecase;

public interface DeleteConceptAssignmentUseCase {

    /**
     * Deletes the concept assignment identified by the given opaque {@code assignmentCode}
     * inside the supplied rule system. The operation is a no-op when no assignment
     * matches; callers wishing to surface 404 must check beforehand.
     */
    void delete(String ruleSystemCode, String assignmentCode);
}
