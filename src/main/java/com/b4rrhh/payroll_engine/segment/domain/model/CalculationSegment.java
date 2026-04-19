package com.b4rrhh.payroll_engine.segment.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A contiguous interval within a CalculationPeriod where employee conditions
 * (e.g. working time percentage) remain constant.
 *
 * <p>Segments inside a period are ordered, contiguous, non-overlapping, and
 * collectively exhaustive: they cover the period from start to end with no gaps.
 *
 * <p>Both {@code segmentStart} and {@code segmentEnd} are inclusive.
 */
public final class CalculationSegment {

    private final LocalDate segmentStart;
    private final LocalDate segmentEnd;
    private final boolean firstSegment;
    private final boolean lastSegment;

    public CalculationSegment(
            LocalDate segmentStart,
            LocalDate segmentEnd,
            boolean firstSegment,
            boolean lastSegment
    ) {
        if (segmentStart == null) {
            throw new IllegalArgumentException("segmentStart is required");
        }
        if (segmentEnd == null) {
            throw new IllegalArgumentException("segmentEnd is required");
        }
        if (segmentEnd.isBefore(segmentStart)) {
            throw new IllegalArgumentException(
                    "segmentEnd must be >= segmentStart. Got start=" + segmentStart + ", end=" + segmentEnd);
        }
        this.segmentStart = segmentStart;
        this.segmentEnd = segmentEnd;
        this.firstSegment = firstSegment;
        this.lastSegment = lastSegment;
    }

    public LocalDate getSegmentStart() {
        return segmentStart;
    }

    public LocalDate getSegmentEnd() {
        return segmentEnd;
    }

    public boolean isFirstSegment() {
        return firstSegment;
    }

    public boolean isLastSegment() {
        return lastSegment;
    }

    /**
     * Returns the number of days in this segment, inclusive of both endpoints.
     * A segment spanning a single day returns 1.
     */
    public long lengthInDaysInclusive() {
        return ChronoUnit.DAYS.between(segmentStart, segmentEnd) + 1;
    }
}
