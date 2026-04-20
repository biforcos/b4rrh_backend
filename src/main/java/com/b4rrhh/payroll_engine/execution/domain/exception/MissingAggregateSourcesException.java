package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

/**
 * Thrown when an AGGREGATE concept has no declared source concepts in the dependency graph.
 *
 * <p>At least one feed relation must point to an AGGREGATE concept so that the
 * plan builder can resolve its contributors. An empty source set means the aggregation
 * has no operands and cannot produce a meaningful result.
 */
public class MissingAggregateSourcesException extends RuntimeException {

    public MissingAggregateSourcesException(ConceptNodeIdentity target) {
        super("AGGREGATE concept '" + target.getRuleSystemCode() + "/" + target.getConceptCode()
                + "' has no source concepts in the dependency graph. "
                + "Add at least one feed relation pointing to this concept.");
    }
}
