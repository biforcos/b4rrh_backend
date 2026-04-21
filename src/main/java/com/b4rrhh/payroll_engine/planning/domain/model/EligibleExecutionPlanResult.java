package com.b4rrhh.payroll_engine.planning.domain.model;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ResolvedConceptAssignment;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;

import java.util.List;

/**
 * The auditable result of building an eligible execution plan for a given employee context.
 *
 * <p>Preserves all intermediate layers so callers can inspect each stage independently:
 * <ol>
 *   <li>{@link #applicableAssignments} — the raw eligibility resolution result: one winning
 *       assignment per applicable concept code, as returned by the eligibility resolver.</li>
 *   <li>{@link #eligibleConcepts} — the concept definitions loaded for the directly applicable
 *       codes. Does NOT include dependencies pulled in during graph expansion.</li>
 *   <li>{@link #expandedConcepts} — the full concept set after transitive dependency expansion.
 *       Includes all structural dependencies (e.g. technical concepts) required for calculation.
 *       These are included because they are structural dependencies, not because eligibility
 *       assigned them directly.</li>
 *   <li>{@link #dependencyGraph} — the structural dependency graph built over the expanded set.</li>
 *   <li>{@link #executionPlan} — the final execution plan in topological order, one entry per
 *       concept in the expanded set.</li>
 * </ol>
 */
public record EligibleExecutionPlanResult(
        List<ResolvedConceptAssignment> applicableAssignments,
        List<PayrollConcept> eligibleConcepts,
        List<PayrollConcept> expandedConcepts,
        ConceptDependencyGraph dependencyGraph,
        List<ConceptExecutionPlanEntry> executionPlan
) {}
