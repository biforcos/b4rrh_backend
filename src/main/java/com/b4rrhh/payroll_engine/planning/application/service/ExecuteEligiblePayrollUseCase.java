package com.b4rrhh.payroll_engine.planning.application.service;

/**
 * Port: executes the payroll engine using eligibility-resolved concepts.
 *
 * <p>Given a {@link EligiblePayrollExecutionRequest}, this use case:
 * <ol>
 *   <li>Builds an {@link com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext}
 *       from the request fields.</li>
 *   <li>Derives {@code referenceDate} as {@code request.periodStart} (first-iteration
 *       convention, intentionally not an explicit request field yet).</li>
 *   <li>Resolves applicable concepts via
 *       {@link BuildEligibleExecutionPlanUseCase}, which expands dependencies and
 *       builds the topological execution plan.</li>
 *   <li>Builds temporal segments from the working time windows in the request.</li>
 *   <li>Executes each segment using the resolved plan (no plan rebuild per segment).</li>
 *   <li>Consolidates period-level totals from segment results.</li>
 * </ol>
 *
 * <p>The result carries all intermediate layers for full auditability.
 */
public interface ExecuteEligiblePayrollUseCase {

    EligiblePayrollExecutionResult execute(EligiblePayrollExecutionRequest request);
}
