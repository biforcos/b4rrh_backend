package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

/**
 * Thrown when a configured operand source concept is not present as a declared graph
 * dependency of the target RATE_BY_QUANTITY concept.
 *
 * <p>This indicates a structural mismatch between the concept dependency graph and the
 * persisted operand configuration. Both must be coherent for correct execution:
 * <ul>
 *   <li>The graph controls calculation order (topological sort).</li>
 *   <li>The operand configuration controls operand wiring (which prior result is used as
 *       QUANTITY and which as RATE).</li>
 * </ul>
 *
 * <p>If this exception is thrown at runtime, it means a concept is configured as an operand
 * but its value cannot be guaranteed to be present in state when the dependent concept
 * executes, because it is not declared as a graph dependency.
 */
public class OperandGraphMismatchException extends RuntimeException {

    public OperandGraphMismatchException(
            String ruleSystemCode,
            String conceptCode,
            OperandRole role,
            ConceptNodeIdentity configuredSource
    ) {
        super("Operand configuration mismatch for concept '" + ruleSystemCode + "/" + conceptCode
                + "': the configured " + role + " source '" + configuredSource.getConceptCode()
                + "' is not a declared graph dependency of the target concept."
                + " Ensure the concept_feed_relation and concept_operand tables are coherent.");
    }
}
