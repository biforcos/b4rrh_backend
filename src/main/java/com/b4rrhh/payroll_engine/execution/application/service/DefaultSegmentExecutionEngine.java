package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedCalculationTypeException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import org.springframework.stereotype.Component;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Default implementation of {@link SegmentExecutionEngine}.
 *
 * <h3>Execution flow</h3>
 * <ol>
 *   <li>Iterates the execution plan in the provided order (must be topological).</li>
 *   <li>For each entry, dispatches based on {@link com.b4rrhh.payroll_engine.concept.domain.model.CalculationType}:
 *       <ul>
 *         <li>{@code DIRECT_AMOUNT} — delegates to {@link SegmentTechnicalValueResolver}</li>
 *         <li>{@code RATE_BY_QUANTITY} — reads pre-resolved QUANTITY and RATE operand
 *             identities from the plan entry, fetches them from state, multiplies
 *             rate × quantity, rounds to 2 decimal places HALF_UP</li>
 *         <li>{@code AGGREGATE} — iterates the pre-resolved source list, sums the stored
 *             amount for each source, rounds to 2 decimal places HALF_UP</li>
 *       </ul>
 *   </li>
 *   <li>Stores each result in {@link SegmentExecutionState}.</li>
 * </ol>
 *
 * <h3>In-memory RATE_BY_QUANTITY execution</h3>
 * <p>For each {@code RATE_BY_QUANTITY} entry, the engine reads the pre-resolved QUANTITY
 * and RATE source identities directly from
 * {@link ConceptExecutionPlanEntry#operands()} and delegates to
 * {@link RateByQuantityOperandResolver}. No repository access occurs at runtime.
 *
 * <h3>Rounding policy</h3>
 * <ul>
 *   <li>Intermediate scale: 8, HALF_UP (delegated to {@link SegmentTechnicalValueResolver})</li>
 *   <li>RATE_BY_QUANTITY final result: scale 2, HALF_UP (delegated to {@link RateByQuantityOperandResolver})</li>
 *   <li>AGGREGATE final result: scale 2, HALF_UP (applied in-engine after summation)</li>
 * </ul>
 */
@Component
public class DefaultSegmentExecutionEngine implements SegmentExecutionEngine {

    private final SegmentTechnicalValueResolver technicalValueResolver;
    private final RateByQuantityOperandResolver rateByQuantityResolver;

    public DefaultSegmentExecutionEngine(
            SegmentTechnicalValueResolver technicalValueResolver,
            RateByQuantityOperandResolver rateByQuantityResolver
    ) {
        this.technicalValueResolver = technicalValueResolver;
        this.rateByQuantityResolver = rateByQuantityResolver;
    }

    @Override
    public SegmentExecutionState execute(
            List<ConceptExecutionPlanEntry> plan,
            SegmentCalculationContext context
    ) {
        SegmentExecutionState state = new SegmentExecutionState();

        for (ConceptExecutionPlanEntry entry : plan) {
            BigDecimal amount = switch (entry.calculationType()) {
                case DIRECT_AMOUNT ->
                        technicalValueResolver.resolve(entry.identity().getConceptCode(), context);

                case RATE_BY_QUANTITY ->
                        rateByQuantityResolver.resolve(entry, state);

                case AGGREGATE -> {
                    BigDecimal sum = BigDecimal.ZERO;
                    for (ConceptNodeIdentity source : entry.aggregateSources()) {
                        sum = sum.add(state.getRequiredAmount(source));
                    }
                    yield sum.setScale(2, RoundingMode.HALF_UP);
                }

                default ->
                        throw new UnsupportedCalculationTypeException(entry.calculationType());
            };

            state.storeResult(entry.identity(), amount);
        }

        return state;
    }
}
