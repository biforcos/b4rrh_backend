package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedCalculationTypeException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link SegmentExecutionEngine}.
 *
 * <h3>Execution flow</h3>
 * <ol>
 *   <li>Iterates the execution plan in the provided order (must be topological).</li>
 *   <li>For each entry, dispatches based on {@link com.b4rrhh.payroll_engine.concept.domain.model.CalculationType}:
 *       <ul>
 *         <li>{@code DIRECT_AMOUNT} — delegates to {@link SegmentTechnicalValueResolver}</li>
 *         <li>{@code RATE_BY_QUANTITY} — validates operand ↔ graph coherence, then reads
 *             previously stored dependencies from state, multiplies rate × quantity,
 *             rounds to 2 decimal places HALF_UP</li>
 *       </ul>
 *   </li>
 *   <li>Stores each result in {@link SegmentExecutionState}.</li>
 * </ol>
 *
 * <h3>Graph ↔ operand validation</h3>
 * <p>For each {@code RATE_BY_QUANTITY} entry, the engine reads the direct graph dependencies
 * of that concept and passes them to {@link RateByQuantityOperandResolver}. The resolver
 * delegates to {@link RateByQuantityConfigurationValidator} to assert that every configured
 * operand source is a declared graph dependency, ensuring state will contain the source
 * value when the multiplication is attempted.
 *
 * <h3>Rounding policy</h3>
 * <ul>
 *   <li>Intermediate scale: 8, HALF_UP (delegated to {@link SegmentTechnicalValueResolver})</li>
 *   <li>RATE_BY_QUANTITY final result: scale 2, HALF_UP (delegated to {@link RateByQuantityOperandResolver})</li>
 * </ul>
 *
 * <h3>Runtime limitation — operand lookup frequency</h3>
 * <p>Operand definitions are currently loaded from the repository on each
 * {@code RATE_BY_QUANTITY} execution (once per concept per segment). This is acceptable
 * for the current PoC but is not the intended optimized runtime model.
 * Future iterations should preload operand configuration at plan-construction time so that
 * per-segment execution operates fully in-memory without additional repository access.
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
            SegmentCalculationContext context,
            ConceptDependencyGraph graph
    ) {
        SegmentExecutionState state = new SegmentExecutionState();

        for (ConceptExecutionPlanEntry entry : plan) {
            BigDecimal amount = switch (entry.calculationType()) {
                case DIRECT_AMOUNT ->
                        technicalValueResolver.resolve(entry.identity().getConceptCode(), context);

                case RATE_BY_QUANTITY -> {
                    Set<ConceptNodeIdentity> graphDeps = graph.getDependenciesOf(entry.identity());
                    yield rateByQuantityResolver.resolve(
                            context.getRuleSystemCode(),
                            entry.identity().getConceptCode(),
                            state,
                            graphDeps);
                }

                default ->
                        throw new UnsupportedCalculationTypeException(entry.calculationType());
            };

            state.storeResult(entry.identity(), amount);
        }

        return state;
    }
}
