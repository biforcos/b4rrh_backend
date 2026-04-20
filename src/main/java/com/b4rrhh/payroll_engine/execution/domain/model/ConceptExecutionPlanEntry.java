package com.b4rrhh.payroll_engine.execution.domain.model;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

import java.util.List;
import java.util.Map;

/**
 * Describes one concept in a segment execution plan.
 *
 * <p>Each entry pairs a concept identity with the calculation type that must be
 * applied when the engine processes that concept. Entries must be provided in
 * topological order: dependencies before dependents.
 *
 * <h3>Operand wiring — RATE_BY_QUANTITY</h3>
 * <p>For {@code RATE_BY_QUANTITY} concepts, {@link #operands()} carries the pre-resolved
 * source identity for each required {@link OperandRole} (QUANTITY and RATE). This wiring
 * is resolved and validated once at plan-construction time by {@link
 * com.b4rrhh.payroll_engine.execution.application.service.DefaultExecutionPlanBuilder},
 * so per-segment execution can operate fully in-memory without repository access.
 * For other calculation types, {@code operands()} is an empty map.
 *
 * <h3>Aggregate sources — AGGREGATE</h3>
 * <p>For {@code AGGREGATE} concepts, {@link #aggregateSources()} carries the ordered list
 * of source concept identities whose computed amounts must be summed within the segment.
 * Sources are resolved from the dependency graph at plan-construction time.
 * For other calculation types, {@code aggregateSources()} is an empty list.
 */
public record ConceptExecutionPlanEntry(
        ConceptNodeIdentity identity,
        CalculationType calculationType,
        Map<OperandRole, ConceptNodeIdentity> operands,
        List<ConceptNodeIdentity> aggregateSources) {

    /**
     * Convenience constructor for entries that do not require operand wiring
     * (e.g. {@code DIRECT_AMOUNT}). Sets {@code operands} to an empty map and
     * {@code aggregateSources} to an empty list.
     */
    public ConceptExecutionPlanEntry(ConceptNodeIdentity identity, CalculationType calculationType) {
        this(identity, calculationType, Map.of(), List.of());
    }

    /**
     * Convenience constructor for {@code RATE_BY_QUANTITY} entries with operand wiring.
     * Sets {@code aggregateSources} to an empty list.
     */
    public ConceptExecutionPlanEntry(
            ConceptNodeIdentity identity,
            CalculationType calculationType,
            Map<OperandRole, ConceptNodeIdentity> operands) {
        this(identity, calculationType, operands, List.of());
    }
}
