package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;

import java.util.List;

/**
 * Port: executes a list of concepts in order for a single temporal segment.
 *
 * <p>The execution plan must be provided in topological order: all dependencies of a concept
 * must appear before that concept in the list.
 *
 * <p>Each concept is evaluated exactly once. Its result is stored in {@link SegmentExecutionState}
 * and is available to subsequent concepts that depend on it.
 *
 * <p>This engine is per-segment only. Period-level consolidation is the responsibility
 * of the caller.
 *
 * <h3>In-memory execution</h3>
 * <p>Plan entries for {@code RATE_BY_QUANTITY} concepts carry pre-resolved operand wiring
 * (QUANTITY and RATE source identities) embedded by the plan builder. Per-segment execution
 * performs no repository access and no graph traversal.
 */
public interface SegmentExecutionEngine {

    /**
     * @param plan    concepts to execute, in topological order (dependencies before dependents);
     *                RATE_BY_QUANTITY entries must carry pre-resolved operand wiring
     * @param context segment context providing technical values
     * @return state containing all calculated concept amounts for the segment
     */
    SegmentExecutionState execute(
            List<ConceptExecutionPlanEntry> plan,
            SegmentCalculationContext context);
}
