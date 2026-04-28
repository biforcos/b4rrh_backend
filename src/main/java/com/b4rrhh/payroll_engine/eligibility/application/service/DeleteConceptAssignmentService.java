package com.b4rrhh.payroll_engine.eligibility.application.service;

import com.b4rrhh.payroll_engine.eligibility.application.usecase.DeleteConceptAssignmentUseCase;
import com.b4rrhh.payroll_engine.eligibility.domain.exception.ConceptAssignmentNotFoundException;
import com.b4rrhh.payroll_engine.eligibility.domain.port.ConceptAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Removes a concept assignment identified by ({@code ruleSystemCode}, {@code assignmentCode}).
 *
 * <p>The OpenAPI contract uses an opaque {@code assignmentCode} string. This iteration
 * adapts that contract to the existing surrogate Long id by parsing the code as a number.
 * A non-numeric or unknown code surfaces as 404 instead of 400 to keep the public contract
 * shape aligned with the spec.
 */
@Service
public class DeleteConceptAssignmentService implements DeleteConceptAssignmentUseCase {

    private final ConceptAssignmentRepository assignmentRepository;

    public DeleteConceptAssignmentService(ConceptAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional
    public void delete(String ruleSystemCode, String assignmentCode) {
        Long id = parseAssignmentCode(ruleSystemCode, assignmentCode);
        if (!assignmentRepository.existsByIdAndRuleSystemCode(id, ruleSystemCode)) {
            throw new ConceptAssignmentNotFoundException(ruleSystemCode, assignmentCode);
        }
        assignmentRepository.deleteById(id);
    }

    private Long parseAssignmentCode(String ruleSystemCode, String assignmentCode) {
        if (assignmentCode == null || assignmentCode.isBlank()) {
            throw new ConceptAssignmentNotFoundException(ruleSystemCode, assignmentCode);
        }
        try {
            return Long.valueOf(assignmentCode.trim());
        } catch (NumberFormatException ex) {
            throw new ConceptAssignmentNotFoundException(ruleSystemCode, assignmentCode);
        }
    }
}
