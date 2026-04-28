package com.b4rrhh.payroll_engine.eligibility.application.service;

import com.b4rrhh.payroll_engine.eligibility.application.usecase.ListConceptAssignmentsUseCase;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ConceptAssignment;
import com.b4rrhh.payroll_engine.eligibility.domain.port.ConceptAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only service that returns every assignment registered under the given rule system,
 * optionally filtered by {@code conceptCode}. Returns an empty list when no row exists;
 * the rule system itself is not validated here because no assignment metadata depends on
 * it for read operations.
 */
@Service
public class ListConceptAssignmentsService implements ListConceptAssignmentsUseCase {

    private final ConceptAssignmentRepository assignmentRepository;

    public ListConceptAssignmentsService(ConceptAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConceptAssignment> list(String ruleSystemCode, String conceptCode) {
        if (conceptCode != null && !conceptCode.isBlank()) {
            return assignmentRepository.findAllByRuleSystemCodeAndConceptCode(ruleSystemCode, conceptCode);
        }
        return assignmentRepository.findAllByRuleSystemCode(ruleSystemCode);
    }
}
