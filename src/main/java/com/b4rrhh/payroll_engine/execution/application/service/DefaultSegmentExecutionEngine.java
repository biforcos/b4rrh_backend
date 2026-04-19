package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedCalculationTypeException;
import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedTechnicalConceptException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import org.springframework.stereotype.Component;

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
 *         <li>{@code RATE_BY_QUANTITY} — reads previously stored dependencies from state,
 *             multiplies rate × quantity, rounds to 2 decimal places HALF_UP</li>
 *       </ul>
 *   </li>
 *   <li>Stores each result in {@link SegmentExecutionState}.</li>
 * </ol>
 *
 * <h3>Supported RATE_BY_QUANTITY concepts (PoC)</h3>
 * <ul>
 *   <li>{@code SALARIO_BASE}: quantity = T_DIAS_PRESENCIA_SEGMENTO, rate = T_PRECIO_DIA</li>
 * </ul>
 *
 * <h3>Rounding policy</h3>
 * <ul>
 *   <li>Intermediate scale: 8, HALF_UP (delegated to {@link SegmentTechnicalValueResolver})</li>
 *   <li>RATE_BY_QUANTITY final result: scale 2, HALF_UP</li>
 * </ul>
 */
@Component
public class DefaultSegmentExecutionEngine implements SegmentExecutionEngine {

    private static final int AMOUNT_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final SegmentTechnicalValueResolver technicalValueResolver;

    public DefaultSegmentExecutionEngine(SegmentTechnicalValueResolver technicalValueResolver) {
        this.technicalValueResolver = technicalValueResolver;
    }

    @Override
    public SegmentExecutionState execute(List<ConceptExecutionPlanEntry> plan, SegmentCalculationContext context) {
        SegmentExecutionState state = new SegmentExecutionState();

        for (ConceptExecutionPlanEntry entry : plan) {
            BigDecimal amount = switch (entry.calculationType()) {
                case DIRECT_AMOUNT ->
                        technicalValueResolver.resolve(entry.identity().getConceptCode(), context);

                case RATE_BY_QUANTITY ->
                        executeRateByQuantity(entry.identity(), context, state);

                default ->
                        throw new UnsupportedCalculationTypeException(entry.calculationType());
            };

            state.storeResult(entry.identity(), amount);
        }

        return state;
    }

    /**
     * Executes a RATE_BY_QUANTITY concept by reading its operand dependencies from state.
     *
     * <p>Supported concepts in this PoC iteration:
     * <ul>
     *   <li>{@code SALARIO_BASE}: quantity × rate
     *       where quantity = T_DIAS_PRESENCIA_SEGMENTO, rate = T_PRECIO_DIA</li>
     * </ul>
     *
     * @throws UnsupportedTechnicalConceptException if the concept code is not recognised
     */
    private BigDecimal executeRateByQuantity(
            ConceptNodeIdentity concept,
            SegmentCalculationContext context,
            SegmentExecutionState state
    ) {
        String ruleSystem = context.getRuleSystemCode();

        return switch (concept.getConceptCode()) {
            case "SALARIO_BASE" -> {
                BigDecimal quantity = state.getRequiredAmount(
                        new ConceptNodeIdentity(ruleSystem, "T_DIAS_PRESENCIA_SEGMENTO"));
                BigDecimal rate = state.getRequiredAmount(
                        new ConceptNodeIdentity(ruleSystem, "T_PRECIO_DIA"));
                yield quantity.multiply(rate).setScale(AMOUNT_SCALE, ROUNDING);
            }
            default ->
                    throw new UnsupportedTechnicalConceptException(concept.getConceptCode());
        };
    }
}
