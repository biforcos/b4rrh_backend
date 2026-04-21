package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.OperandGraphMismatchException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Validates structural coherence between persisted operand configuration and the
 * concept dependency graph for operand-wired concepts.
 *
 * <h3>Invariant enforced</h3>
 * <p>Every operand source concept must be declared as a direct or transitive dependency of
 * the target concept in the graph. This guarantees that when the target executes, all operand
 * source values have already been computed and stored in
 * {@link com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState}.
 *
 * <h3>Supported calculation types</h3>
 * <ul>
 *   <li>{@code RATE_BY_QUANTITY} — operand roles QUANTITY and RATE must both be declared
 *       as graph dependencies of the target concept.</li>
 *   <li>{@code PERCENTAGE} — operand roles BASE and PERCENTAGE must both be declared
 *       as graph dependencies of the target concept.</li>
 * </ul>
 *
 * <h3>Scope</h3>
 * <p>This validator only checks graph ↔ operand alignment. It does not validate:
 * <ul>
 *   <li>presence of required operand roles (done by the per-type resolver)</li>
 *   <li>uniqueness of operand roles per concept (done by the per-type resolver)</li>
 *   <li>numeric calculations</li>
 * </ul>
 *
 * <h3>When to call</h3>
 * <p>This validator is called per operand-wired concept at plan-construction time,
 * after operand definitions are loaded and before any state reads.
 */
@Component
public class OperandConfigurationValidator {

    /**
     * Validates that each operand source in {@code operands} is present among the declared
     * graph dependencies of the target concept.
     *
     * @param ruleSystemCode   rule system of the target concept
     * @param conceptCode      code of the target concept being validated
     * @param operands         loaded operand definitions for the target concept
     * @param graphDependencies direct (and transitive) dependencies of the target in the graph —
     *                          i.e. concepts that must be calculated before the target
     * @throws OperandGraphMismatchException if any operand source is absent from the graph
     *                                       dependencies
     */
    public void validate(
            String ruleSystemCode,
            String conceptCode,
            List<PayrollConceptOperand> operands,
            Set<ConceptNodeIdentity> graphDependencies
    ) {
        for (PayrollConceptOperand operand : operands) {
            ConceptNodeIdentity sourceId = new ConceptNodeIdentity(
                    ruleSystemCode, operand.getSourceObject().getObjectCode());

            if (!graphDependencies.contains(sourceId)) {
                throw new OperandGraphMismatchException(
                        ruleSystemCode, conceptCode, operand.getOperandRole(), sourceId);
            }
        }
    }
}
