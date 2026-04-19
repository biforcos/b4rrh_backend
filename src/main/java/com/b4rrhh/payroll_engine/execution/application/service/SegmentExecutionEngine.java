package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
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
 * <h3>Graph and operand coherence</h3>
 * <p>The {@link ConceptDependencyGraph} is passed at execution time so that the engine can
 * validate, for each {@code RATE_BY_QUANTITY} concept, that its configured operand sources
 * are declared graph dependencies. This prevents silent miscalculations caused by operand
 * configuration drifting out of sync with the graph structure.
 *
 * <p>Future optimization note: graph-operand coherence validation is currently performed
 * per-segment. Once plans are pre-built and cached, this check should be moved to
 * plan-construction time.
 */
public interface SegmentExecutionEngine {

    /**
     * @param plan    concepts to execute, in topological order (dependencies before dependents)
     * @param context segment context providing technical values
     * @param graph   the concept dependency graph used for operand coherence validation
     * @return state containing all calculated concept amounts for the segment
     */
    SegmentExecutionState execute(
            List<ConceptExecutionPlanEntry> plan,
            SegmentCalculationContext context,
            ConceptDependencyGraph graph);
}
