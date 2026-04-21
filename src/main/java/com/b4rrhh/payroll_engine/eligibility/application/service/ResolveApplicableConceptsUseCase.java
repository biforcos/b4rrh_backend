package com.b4rrhh.payroll_engine.eligibility.application.service;

import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ResolvedConceptAssignment;

import java.time.LocalDate;
import java.util.List;

/**
 * Input port for resolving the set of applicable payroll concepts for a given
 * employee context and reference date.
 *
 * <p>The returned list contains exactly one {@link ResolvedConceptAssignment} per
 * applicable concept (the winner of the priority resolution).
 *
 * <p>Results are sorted by:
 * <ol>
 *   <li>priority descending</li>
 *   <li>conceptCode ascending (for deterministic output)</li>
 * </ol>
 *
 * @throws com.b4rrhh.payroll_engine.eligibility.domain.exception.DuplicateConceptAssignmentException
 *         if two assignments for the same conceptCode share the same highest priority
 */
public interface ResolveApplicableConceptsUseCase {

    List<ResolvedConceptAssignment> resolve(EmployeeAssignmentContext context, LocalDate referenceDate);
}
