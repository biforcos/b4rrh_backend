package com.b4rrhh.employee.temporal.support;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {

    public DateRange {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be greater than or equal to startDate");
        }
    }

    public LocalDate effectiveEnd(LocalDate maxDate) {
        if (maxDate == null) {
            throw new IllegalArgumentException("maxDate is required");
        }

        return endDate == null ? maxDate : endDate;
    }

    public boolean contains(DateRange other, LocalDate maxDate) {
        if (other == null) {
            throw new IllegalArgumentException("other is required");
        }

        LocalDate thisEnd = effectiveEnd(maxDate);
        LocalDate otherEnd = other.effectiveEnd(maxDate);

        return !other.startDate.isBefore(startDate)
                && !otherEnd.isAfter(thisEnd);
    }

    public boolean overlaps(DateRange other, LocalDate maxDate) {
        if (other == null) {
            throw new IllegalArgumentException("other is required");
        }

        LocalDate thisEnd = effectiveEnd(maxDate);
        LocalDate otherEnd = other.effectiveEnd(maxDate);

        return !thisEnd.isBefore(other.startDate)
                && !otherEnd.isBefore(startDate);
    }
}
