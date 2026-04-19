package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

/**
 * Thrown when the execution plan builder finds a node in the dependency graph
 * that has no matching {@code PayrollConcept} in the supplied concept list.
 *
 * <p>This is a fail-fast guard: a node without a concept definition cannot
 * contribute a {@code CalculationType} to the execution plan.
 */
public class MissingConceptDefinitionException extends RuntimeException {

    public MissingConceptDefinitionException(ConceptNodeIdentity identity) {
        super("No PayrollConcept definition found for graph node: " + identity +
              ". Every node in the graph must have a matching concept in the supplied list.");
    }
}
