package com.b4rrhh.payroll_engine.execution.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consolidated result of a full payroll engine PoC execution.
 *
 * <p>These are PoC-specific named extractions, not yet a generic result projection.
 *
 * <ul>
 *   <li>{@code totalSalarioBase} — period-level sum of all per-segment {@code salarioBaseAmount}
 *       values (SALARIO_BASE concept, RATE_BY_QUANTITY).</li>
 *   <li>{@code totalPlusTransporte} — period-level sum of all per-segment
 *       {@code plusTransporteAmount} values (PLUS_TRANSPORTE concept, RATE_BY_QUANTITY).</li>
 *   <li>{@code totalDevengosConsolidated} — period-level sum of all per-segment
 *       {@code totalDevengosSegmentoAmount} values, i.e. the period-level consolidation
 *       of the AGGREGATE concept TOTAL_DEVENGOS_SEGMENTO.</li>
 *   <li>{@code totalRetencionIrpf} — period-level sum of all per-segment
 *       {@code retencionIrpfTramoAmount} values (PERCENTAGE concept RETENCION_IRPF_TRAMO).
 *       PoC: flat rate; not a real tax computation.</li>
 * </ul>
 */
public final class PayrollEnginePocResult {

    private final List<SegmentExecutionResult> segmentResults;
    private final BigDecimal totalSalarioBase;
    private final BigDecimal totalPlusTransporte;
    private final BigDecimal totalDevengosConsolidated;
    private final BigDecimal totalRetencionIrpf;

    public PayrollEnginePocResult(
            List<SegmentExecutionResult> segmentResults,
            BigDecimal totalSalarioBase,
            BigDecimal totalPlusTransporte,
            BigDecimal totalDevengosConsolidated,
            BigDecimal totalRetencionIrpf) {
        this.segmentResults = List.copyOf(segmentResults);
        this.totalSalarioBase = totalSalarioBase;
        this.totalPlusTransporte = totalPlusTransporte;
        this.totalDevengosConsolidated = totalDevengosConsolidated;
        this.totalRetencionIrpf = totalRetencionIrpf;
    }

    public List<SegmentExecutionResult> getSegmentResults() { return segmentResults; }
    public BigDecimal getTotalSalarioBase() { return totalSalarioBase; }
    public BigDecimal getTotalPlusTransporte() { return totalPlusTransporte; }
    public BigDecimal getTotalDevengosConsolidated() { return totalDevengosConsolidated; }
    public BigDecimal getTotalRetencionIrpf() { return totalRetencionIrpf; }
}
