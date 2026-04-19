package com.b4rrhh.payroll_engine.execution.domain.model;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptResultException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Accumulates calculated concept amounts during one segment execution.
 *
 * <p>Results are stored as concepts are evaluated in topological order.
 * Calling {@link #getRequiredAmount} for a concept whose result has not yet been
 * stored leads to a {@link MissingConceptResultException} fail-fast.
 *
 * <p>This state is per-segment and must not be reused across segments.
 */
public final class SegmentExecutionState {

    private final Map<ConceptNodeIdentity, BigDecimal> results = new LinkedHashMap<>();

    /**
     * Stores the calculated amount for a concept.
     *
     * @throws IllegalArgumentException if the same concept is stored twice
     */
    public void storeResult(ConceptNodeIdentity concept, BigDecimal amount) {
        if (results.containsKey(concept)) {
            throw new IllegalArgumentException(
                    "Concept result already stored for " + concept + ". Each concept must be evaluated once.");
        }
        results.put(concept, amount);
    }

    /**
     * Returns the amount previously stored for the given concept.
     *
     * @throws MissingConceptResultException if the concept has not been evaluated yet
     */
    public BigDecimal getRequiredAmount(ConceptNodeIdentity concept) {
        BigDecimal value = results.get(concept);
        if (value == null) {
            throw new MissingConceptResultException(
                    "Required concept result not yet computed: " + concept +
                    ". Ensure the concept appears before its dependents in the execution plan.");
        }
        return value;
    }

    /**
     * Returns the amount for the given concept if already computed, or empty otherwise.
     */
    public Optional<BigDecimal> getOptionalAmount(ConceptNodeIdentity concept) {
        return Optional.ofNullable(results.get(concept));
    }
}
