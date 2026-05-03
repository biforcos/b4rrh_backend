package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingPlannedOperandException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Computes the result of a {@code GREATEST} execution: returns the larger of two operands.
 *
 * <h3>Formula</h3>
 * <pre>
 *   result = max(left, right)
 * </pre>
 *
 * <h3>Contract</h3>
 * <p>Operand source identities ({@code LEFT} and {@code RIGHT}) must have been resolved
 * and embedded into the {@link ConceptExecutionPlanEntry#operands()} map by
 * {@link DefaultExecutionPlanBuilder} at plan-construction time. Per-segment execution
 * performs no repository access.
 */
@Component
public class GreatestConceptResolver {

    public BigDecimal resolve(ConceptExecutionPlanEntry entry, SegmentExecutionState state) {
        ConceptNodeIdentity leftId  = getPlannedOperand(entry, OperandRole.LEFT);
        ConceptNodeIdentity rightId = getPlannedOperand(entry, OperandRole.RIGHT);
        return state.getRequiredAmount(leftId).max(state.getRequiredAmount(rightId));
    }

    private ConceptNodeIdentity getPlannedOperand(ConceptExecutionPlanEntry entry, OperandRole role) {
        ConceptNodeIdentity source = entry.operands().get(role);
        if (source == null) {
            throw new MissingPlannedOperandException(entry.identity(), role);
        }
        return source;
    }
}
