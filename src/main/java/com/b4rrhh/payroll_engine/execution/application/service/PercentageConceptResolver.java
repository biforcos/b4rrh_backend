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
 * Computes the result of a {@code PERCENTAGE} execution from pre-resolved operand
 * wiring embedded in the plan entry and pre-computed amounts in the segment execution state.
 *
 * <h3>Formula</h3>
 * <pre>
 *   result = base × percentage / 100
 * </pre>
 *
 * <h3>Contract</h3>
 * <p>Operand source identities ({@code BASE} and {@code PERCENTAGE}) must have been resolved
 * and embedded into the {@link ConceptExecutionPlanEntry#operands()} map by
 * {@link DefaultExecutionPlanBuilder} at plan-construction time. Per-segment execution
 * performs no repository access and no graph traversal.
 *
 * <h3>Fail-fast contract</h3>
 * <ul>
 *   <li>If the plan entry lacks operand wiring for {@code BASE} or {@code PERCENTAGE},
 *       {@link MissingPlannedOperandException} is thrown — plan-construction defect.</li>
 *   <li>If a required source amount is absent from state,
 *       {@link com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptResultException}
 *       is thrown via {@link SegmentExecutionState#getRequiredAmount} — plan not topological.</li>
 * </ul>
 *
 * <h3>Rounding</h3>
 * <p>Intermediate division by 100 uses scale 8, HALF_UP. Final result is rounded to scale 2, HALF_UP.
 */
@Component
public class PercentageConceptResolver {

    private static final int INTERMEDIATE_SCALE = 8;
    private static final int AMOUNT_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Computes {@code base × percentage / 100} for the given plan entry using amounts from state.
     *
     * @param entry plan entry for the PERCENTAGE concept; must carry BASE and PERCENTAGE
     *              operand wiring in {@link ConceptExecutionPlanEntry#operands()}
     * @param state current segment state; must already contain source concept amounts
     * @return {@code base × percentage / 100}, rounded to scale 2 HALF_UP
     * @throws MissingPlannedOperandException if operand wiring is absent from the entry
     */
    public BigDecimal resolve(ConceptExecutionPlanEntry entry, SegmentExecutionState state) {
        ConceptNodeIdentity baseId       = getPlannedOperand(entry, OperandRole.BASE);
        ConceptNodeIdentity percentageId = getPlannedOperand(entry, OperandRole.PERCENTAGE);

        BigDecimal base       = state.getRequiredAmount(baseId);
        BigDecimal percentage = state.getRequiredAmount(percentageId);

        return base.multiply(percentage)
                .divide(BigDecimal.valueOf(100), INTERMEDIATE_SCALE, ROUNDING)
                .setScale(AMOUNT_SCALE, ROUNDING);
    }

    private ConceptNodeIdentity getPlannedOperand(ConceptExecutionPlanEntry entry, OperandRole role) {
        ConceptNodeIdentity source = entry.operands().get(role);
        if (source == null) {
            throw new MissingPlannedOperandException(entry.identity(), role);
        }
        return source;
    }
}
