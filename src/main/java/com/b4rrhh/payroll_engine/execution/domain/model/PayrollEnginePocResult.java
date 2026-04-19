package com.b4rrhh.payroll_engine.execution.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consolidated result of a full payroll engine PoC execution.
 *
 * <p>{@code totalDevengos} is the sum of all per-segment {@code salarioBaseAmount} values.
 * In this PoC it represents TOTAL_DEVENGOS as produced by consolidation,
 * not by full aggregate runtime execution.
 */
public final class PayrollEnginePocResult {

    private final List<SegmentExecutionResult> segmentResults;
    private final BigDecimal totalDevengos;

    public PayrollEnginePocResult(List<SegmentExecutionResult> segmentResults, BigDecimal totalDevengos) {
        this.segmentResults = List.copyOf(segmentResults);
        this.totalDevengos = totalDevengos;
    }

    public List<SegmentExecutionResult> getSegmentResults() { return segmentResults; }
    public BigDecimal getTotalDevengos() { return totalDevengos; }
}
