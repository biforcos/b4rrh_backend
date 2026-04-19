package com.b4rrhh.payroll_engine.segment.domain.model;

import java.time.LocalDate;

/**
 * Represents the full payroll calculation period (typically one calendar month).
 *
 * <p>The period defines the outer boundary within which segments are computed.
 * Both {@code periodStart} and {@code periodEnd} are inclusive.
 */
public final class CalculationPeriod {

    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    public CalculationPeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null) {
            throw new IllegalArgumentException("periodStart is required");
        }
        if (periodEnd == null) {
            throw new IllegalArgumentException("periodEnd is required");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException(
                    "periodEnd must be >= periodStart. Got start=" + periodStart + ", end=" + periodEnd);
        }
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }
}
