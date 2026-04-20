package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingPlannedOperandException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes the result of a {@code RATE_BY_QUANTITY} execution from pre-resolved operand
 * wiring embedded in the plan entry and pre-computed amounts in the segment execution state.
 *
 * <h3>Contract</h3>
 * <p>Operand source identities (QUANTITY and RATE) must have been resolved and embedded into
 * the {@link ConceptExecutionPlanEntry#operands()} map by
 * {@link DefaultExecutionPlanBuilder} at plan-construction time. Per-segment execution
 * performs no repository access and no graph traversal.
 *
 * <h3>Fail-fast contract</h3>
 * <ul>
 *   <li>If the plan entry lacks operand wiring for QUANTITY or RATE, {@link MissingPlannedOperandException}
 *       is thrown — this indicates a plan-construction defect.</li>
 *   <li>If a required source amount is absent from state, {@link
 *       com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptResultException} is thrown via
 *       {@link SegmentExecutionState#getRequiredAmount} — this indicates the plan was not
 *       in topological order.</li>
 * </ul>
 *
 * <h3>Rounding</h3>
 * <p>Result is rounded to scale 2, HALF_UP after multiplying quantity × rate.
 */
@Component
public class RateByQuantityOperandResolver {

    private static final int AMOUNT_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Computes quantity × rate for the given plan entry using amounts from execution state.
     *
     * @param entry plan entry for the RATE_BY_QUANTITY concept; must carry QUANTITY and RATE
     *              operand wiring in {@link ConceptExecutionPlanEntry#operands()}
     * @param state current segment state; must already contain source concept amounts
     * @return quantity × rate, rounded to scale 2 HALF_UP
     * @throws MissingPlannedOperandException if operand wiring is absent from the entry
     */
    public BigDecimal resolve(ConceptExecutionPlanEntry entry, SegmentExecutionState state) {
        ConceptNodeIdentity quantityId = getPlannedOperand(entry, OperandRole.QUANTITY);
        ConceptNodeIdentity rateId     = getPlannedOperand(entry, OperandRole.RATE);

        BigDecimal quantity = state.getRequiredAmount(quantityId);
        BigDecimal rate     = state.getRequiredAmount(rateId);

        return quantity.multiply(rate).setScale(AMOUNT_SCALE, ROUNDING);
    }

    private ConceptNodeIdentity getPlannedOperand(ConceptExecutionPlanEntry entry, OperandRole role) {
        ConceptNodeIdentity source = entry.operands().get(role);
        if (source == null) {
            throw new MissingPlannedOperandException(entry.identity(), role);
        }
        return source;
    }
}
