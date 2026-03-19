package com.b4rrhh.employee.temporal.support;

import java.time.LocalDate;

public final class TemporalDates {

    public static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private TemporalDates() {
    }

    public static LocalDate effectiveEnd(LocalDate endDate) {
        return endDate == null ? MAX_DATE : endDate;
    }

    public static LocalDate previousDay(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        return date.minusDays(1);
    }

    public static LocalDate nextDay(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }
        if (MAX_DATE.equals(date)) {
            return MAX_DATE;
        }

        return date.plusDays(1);
    }
}
