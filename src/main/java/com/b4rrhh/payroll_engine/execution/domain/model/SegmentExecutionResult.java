package com.b4rrhh.payroll_engine.execution.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Result of executing the payroll engine PoC for a single temporal segment.
 *
 * <p>All monetary amounts are expressed in the same currency as the input
 * monthlySalaryAmount and are rounded to 2 decimal places (HALF_UP).
 *
 * <p>These fields are PoC-specific named extractions from the segment execution state.
 * Each field corresponds to a specific concept computed during segment execution:
 * <ul>
 *   <li>{@code dailyRate} — amount of {@code T_PRECIO_DIA} (DIRECT_AMOUNT): working-time-scaled
 *       daily rate, computed as {@code monthlySalary / daysInPeriod * workingTimeFactor}.</li>
 *   <li>{@code salarioBaseAmount} — amount of {@code SALARIO_BASE} (RATE_BY_QUANTITY):
 *       {@code T_DIAS_PRESENCIA_SEGMENTO * T_PRECIO_DIA}.</li>
 *   <li>{@code plusTransporteAmount} — amount of {@code PLUS_TRANSPORTE} (RATE_BY_QUANTITY):
 *       {@code T_DIAS_PRESENCIA_SEGMENTO * T_PRECIO_TRANSPORTE}.</li>
 *   <li>{@code totalDevengosSegmentoAmount} — amount of {@code TOTAL_DEVENGOS_SEGMENTO}
 *       (AGGREGATE): {@code SALARIO_BASE + PLUS_TRANSPORTE} for this segment.</li>
 * </ul>
 */
public final class SegmentExecutionResult {

    private final LocalDate segmentStart;
    private final LocalDate segmentEnd;
    private final boolean firstSegment;
    private final boolean lastSegment;
    private final long daysInPeriod;
    private final long daysInSegment;
    private final BigDecimal workingTimePercentage;
    private final BigDecimal dailyRate;
    private final BigDecimal salarioBaseAmount;
    private final BigDecimal plusTransporteAmount;
    private final BigDecimal totalDevengosSegmentoAmount;

    public SegmentExecutionResult(
            LocalDate segmentStart,
            LocalDate segmentEnd,
            boolean firstSegment,
            boolean lastSegment,
            long daysInPeriod,
            long daysInSegment,
            BigDecimal workingTimePercentage,
            BigDecimal dailyRate,
            BigDecimal salarioBaseAmount,
            BigDecimal plusTransporteAmount,
            BigDecimal totalDevengosSegmentoAmount
    ) {
        this.segmentStart = segmentStart;
        this.segmentEnd = segmentEnd;
        this.firstSegment = firstSegment;
        this.lastSegment = lastSegment;
        this.daysInPeriod = daysInPeriod;
        this.daysInSegment = daysInSegment;
        this.workingTimePercentage = workingTimePercentage;
        this.dailyRate = dailyRate;
        this.salarioBaseAmount = salarioBaseAmount;
        this.plusTransporteAmount = plusTransporteAmount;
        this.totalDevengosSegmentoAmount = totalDevengosSegmentoAmount;
    }

    public LocalDate getSegmentStart() { return segmentStart; }
    public LocalDate getSegmentEnd() { return segmentEnd; }
    public boolean isFirstSegment() { return firstSegment; }
    public boolean isLastSegment() { return lastSegment; }
    public long getDaysInPeriod() { return daysInPeriod; }
    public long getDaysInSegment() { return daysInSegment; }
    public BigDecimal getWorkingTimePercentage() { return workingTimePercentage; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public BigDecimal getSalarioBaseAmount() { return salarioBaseAmount; }
    public BigDecimal getPlusTransporteAmount() { return plusTransporteAmount; }
    public BigDecimal getTotalDevengosSegmentoAmount() { return totalDevengosSegmentoAmount; }
}
