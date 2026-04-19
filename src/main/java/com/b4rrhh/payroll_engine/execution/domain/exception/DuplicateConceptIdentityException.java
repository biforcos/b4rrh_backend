package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

/**
 * Thrown when the execution plan builder detects that the supplied concept list
 * contains two or more {@code PayrollConcept} entries with the same
 * {@code (ruleSystemCode, conceptCode)} identity.
 *
 * <p>Duplicate concept identities are ambiguous: the builder cannot determine
 * which {@code CalculationType} to assign to the corresponding graph node.
 */
public class DuplicateConceptIdentityException extends RuntimeException {

    public DuplicateConceptIdentityException(ConceptNodeIdentity identity) {
        super("Duplicate PayrollConcept identity in the supplied concept list: " + identity +
              ". Each concept must appear exactly once.");
    }
}
