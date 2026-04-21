package com.b4rrhh.payroll_engine.planning.application.service;

import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.planning.domain.model.EligibleExecutionPlanResult;

import java.time.LocalDate;

/**
 * Input port for building an eligible execution plan from an employee context and reference date.
 *
 * <p>The result includes all intermediate layers (applicable assignments, eligible concepts,
 * expanded concepts, dependency graph, execution plan) for full auditability.
 *
 * <p>This use case integrates:
 * <ol>
 *   <li>Eligibility resolution — which concepts apply from a business perspective.</li>
 *   <li>Concept definition loading — the structural definition of each applicable concept.</li>
 *   <li>Dependency expansion — transitive inclusion of all structurally required upstream
 *       concepts (e.g. technical concepts such as T_DIAS_PRESENCIA_SEGMENTO).</li>
 *   <li>Execution plan construction — topologically ordered plan ready for the execution engine.</li>
 * </ol>
 *
 * <p>This use case does NOT execute the payroll calculation. It only prepares the plan.
 */
public interface BuildEligibleExecutionPlanUseCase {

    /**
     * Builds the eligible execution plan for the given context and reference date.
     *
     * @param context       the employee context carrying rule system and optional scope dimensions
     * @param referenceDate the date used for eligibility validity and feed relation filtering
     * @return an auditable result with all intermediate layers
     */
    EligibleExecutionPlanResult build(EmployeeAssignmentContext context, LocalDate referenceDate);
}
