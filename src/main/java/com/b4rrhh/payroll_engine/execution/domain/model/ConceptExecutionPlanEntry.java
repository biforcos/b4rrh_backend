package com.b4rrhh.payroll_engine.execution.domain.model;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

/**
 * Describes one concept in a segment execution plan.
 *
 * <p>Each entry pairs a concept identity with the calculation type that must be
 * applied when the engine processes that concept. Entries must be provided in
 * topological order: dependencies before dependents.
 */
public record ConceptExecutionPlanEntry(
        ConceptNodeIdentity identity,
        CalculationType calculationType
) {}
