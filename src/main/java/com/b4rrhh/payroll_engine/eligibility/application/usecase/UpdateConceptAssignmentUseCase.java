package com.b4rrhh.payroll_engine.eligibility.application.usecase;

import com.b4rrhh.payroll_engine.eligibility.domain.model.ConceptAssignment;

public interface UpdateConceptAssignmentUseCase {
    ConceptAssignment update(UpdateConceptAssignmentCommand command);
}
