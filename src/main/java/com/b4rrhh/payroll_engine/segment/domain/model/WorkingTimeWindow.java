package com.b4rrhh.payroll_engine.segment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a declared interval of working time for an employee.
 *
 * <p>{@code startDate} is the first day on which {@code workingTimePercentage} applies.
 * {@code endDate} is the last day of validity; if null the window is open-ended.
 *
 * <p>When building segments, an open-ended window is clipped to the period end.
 */
public final class WorkingTimeWindow {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal workingTimePercentage;

    public WorkingTimeWindow(
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal workingTimePercentage
    ) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        if (workingTimePercentage == null) {
            throw new IllegalArgumentException("workingTimePercentage is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "endDate must be >= startDate. Got start=" + startDate + ", end=" + endDate);
        }
        this.startDate = startDate;
        this.endDate = endDate;
        this.workingTimePercentage = workingTimePercentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    /** Returns the declared end date, or null if this window is open-ended. */
    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getWorkingTimePercentage() {
        return workingTimePercentage;
    }
}
