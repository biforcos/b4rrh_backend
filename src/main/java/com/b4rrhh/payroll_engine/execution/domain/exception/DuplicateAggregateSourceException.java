package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

/**
 * Thrown when the same source concept appears more than once in the resolved aggregate
 * source list for an AGGREGATE concept.
 *
 * <p>The dependency graph uses a {@code Set} of identities, so duplicates are structurally
 * prevented during normal operation. This exception acts as a defensive guard in case the
 * graph is built with raw data that bypasses the set deduplication.
 */
public class DuplicateAggregateSourceException extends RuntimeException {

    public DuplicateAggregateSourceException(ConceptNodeIdentity target, ConceptNodeIdentity source) {
        super("AGGREGATE concept '" + target.getRuleSystemCode() + "/" + target.getConceptCode()
                + "' has duplicate source '" + source.getRuleSystemCode() + "/" + source.getConceptCode()
                + "'. Each source concept must appear at most once in the aggregate.");
    }
}
