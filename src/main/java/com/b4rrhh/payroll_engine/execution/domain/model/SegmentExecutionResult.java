package com.b4rrhh.payroll_engine.execution.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Result of executing the payroll engine PoC for a single temporal segment.
 *
 * <p>All monetary amounts are expressed in the same currency as the input
 * monthlySalaryAmount. Amounts are rounded to 2 decimal places (HALF_UP).
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

    public SegmentExecutionResult(
            LocalDate segmentStart,
            LocalDate segmentEnd,
            boolean firstSegment,
            boolean lastSegment,
            long daysInPeriod,
            long daysInSegment,
            BigDecimal workingTimePercentage,
            BigDecimal dailyRate,
            BigDecimal salarioBaseAmount
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
}
