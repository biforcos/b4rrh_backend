package com.b4rrhh.payroll_engine.planning.application.service;

import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionResult;
import com.b4rrhh.payroll_engine.planning.domain.model.EligibleExecutionPlanResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Auditable result of the eligible payroll execution use case.
 *
 * <p>Preserves all intermediate layers for full observability:
 * <ul>
 *   <li>{@link #context} — the eligibility context derived from the request</li>
 *   <li>{@link #referenceDate} — the date used for eligibility and feed relation filtering</li>
 *   <li>{@link #planningResult} — the full eligible execution plan including applicable
 *       assignments, expanded concepts, dependency graph, and ordered plan entries</li>
 *   <li>{@link #segmentResults} — per-segment execution results (PoC-specific named fields)</li>
 *   <li>period-level totals — consolidated from segment results</li>
 * </ul>
 */
public final class EligiblePayrollExecutionResult {

    private final EmployeeAssignmentContext context;
    private final LocalDate referenceDate;
    private final EligibleExecutionPlanResult planningResult;
    private final List<SegmentExecutionResult> segmentResults;
    private final BigDecimal totalSalarioBase;
    private final BigDecimal totalPlusTransporte;
    private final BigDecimal totalDevengosConsolidated;
    private final BigDecimal totalRetencionIrpf;

    public EligiblePayrollExecutionResult(
            EmployeeAssignmentContext context,
            LocalDate referenceDate,
            EligibleExecutionPlanResult planningResult,
            List<SegmentExecutionResult> segmentResults,
            BigDecimal totalSalarioBase,
            BigDecimal totalPlusTransporte,
            BigDecimal totalDevengosConsolidated,
            BigDecimal totalRetencionIrpf
    ) {
        this.context = context;
        this.referenceDate = referenceDate;
        this.planningResult = planningResult;
        this.segmentResults = List.copyOf(segmentResults);
        this.totalSalarioBase = totalSalarioBase;
        this.totalPlusTransporte = totalPlusTransporte;
        this.totalDevengosConsolidated = totalDevengosConsolidated;
        this.totalRetencionIrpf = totalRetencionIrpf;
    }

    public EmployeeAssignmentContext getContext() { return context; }
    public LocalDate getReferenceDate() { return referenceDate; }
    public EligibleExecutionPlanResult getPlanningResult() { return planningResult; }
    public List<SegmentExecutionResult> getSegmentResults() { return segmentResults; }
    public BigDecimal getTotalSalarioBase() { return totalSalarioBase; }
    public BigDecimal getTotalPlusTransporte() { return totalPlusTransporte; }
    public BigDecimal getTotalDevengosConsolidated() { return totalDevengosConsolidated; }
    public BigDecimal getTotalRetencionIrpf() { return totalRetencionIrpf; }
}
