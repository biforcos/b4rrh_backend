package com.b4rrhh.payroll_engine.eligibility.application.usecase;

import com.b4rrhh.payroll_engine.eligibility.domain.model.ConceptAssignment;

import java.util.List;

public interface ListConceptAssignmentsUseCase {

    /**
     * Returns the assignments registered under the rule system. When {@code conceptCode}
     * is non-null, results are restricted to assignments matching that concept code; the
     * rule system itself is always required.
     */
    List<ConceptAssignment> list(String ruleSystemCode, String conceptCode);
}
