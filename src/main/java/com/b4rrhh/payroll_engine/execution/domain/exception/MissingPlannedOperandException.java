package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

/**
 * Thrown when the segment execution engine encounters a {@code RATE_BY_QUANTITY} plan entry
 * that is missing the required operand wiring for a given {@link OperandRole}.
 *
 * <p>This exception indicates a plan-construction defect: the entry was not enriched with
 * operand sources before being passed to the engine. This should never occur when
 * {@link com.b4rrhh.payroll_engine.execution.application.service.DefaultExecutionPlanBuilder}
 * is used correctly.
 */
public class MissingPlannedOperandException extends RuntimeException {

    public MissingPlannedOperandException(ConceptNodeIdentity target, OperandRole role) {
        super("Plan entry for concept '" + target.getRuleSystemCode() + "/" + target.getConceptCode()
                + "' is missing planned operand wiring for role " + role
                + ". Ensure the plan was built with DefaultExecutionPlanBuilder.");
    }
}
