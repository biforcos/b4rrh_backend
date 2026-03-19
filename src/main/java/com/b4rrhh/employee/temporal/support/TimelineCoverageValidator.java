package com.b4rrhh.employee.temporal.support;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Technical helper for timeline coverage validation.
 * Does not contain domain logic or throw business exceptions.
 */
public final class TimelineCoverageValidator {

    public boolean isContained(List<DateRange> subjectPeriods, List<DateRange> requiredPeriods) {
        List<DateRange> subjects = sorted(subjectPeriods);
        List<DateRange> required = sorted(requiredPeriods);

        if (required.isEmpty()) {
            return subjects.isEmpty();
        }

        for (DateRange subject : subjects) {
            boolean contained = false;
            for (DateRange requiredPeriod : required) {
                if (requiredPeriod.contains(subject, TemporalDates.MAX_DATE)) {
                    contained = true;
                    break;
                }
            }

            if (!contained) {
                return false;
            }
        }

        return true;
    }

    public boolean isFullyCovered(List<DateRange> subjectPeriods, List<DateRange> requiredPeriods) {
        List<DateRange> subjects = sorted(subjectPeriods);
        List<DateRange> required = sorted(requiredPeriods);

        if (required.isEmpty()) {
            return subjects.isEmpty();
        }

        for (DateRange requiredPeriod : required) {
            LocalDate requiredStart = requiredPeriod.startDate();
            LocalDate requiredEnd = requiredPeriod.effectiveEnd(TemporalDates.MAX_DATE);
            LocalDate cursor = requiredStart;

            for (DateRange subject : subjects) {
                if (!subject.overlaps(requiredPeriod, TemporalDates.MAX_DATE)) {
                    continue;
                }

                LocalDate effectiveStart = subject.startDate().isBefore(requiredStart)
                        ? requiredStart
                        : subject.startDate();
                LocalDate effectiveEnd = subject.effectiveEnd(TemporalDates.MAX_DATE).isAfter(requiredEnd)
                        ? requiredEnd
                        : subject.effectiveEnd(TemporalDates.MAX_DATE);

                if (effectiveStart.isAfter(cursor)) {
                    return false;
                }

                if (!effectiveEnd.isBefore(cursor)) {
                    cursor = TemporalDates.nextDay(effectiveEnd);
                }

                if (isCursorBeyondEnd(cursor, requiredEnd)) {
                    break;
                }
            }

            if (!isCursorBeyondEnd(cursor, requiredEnd)) {
                return false;
            }
        }

        return true;
    }

    private List<DateRange> sorted(List<DateRange> periods) {
        if (periods == null) {
            throw new IllegalArgumentException("periods is required");
        }

        List<DateRange> sorted = new ArrayList<>(periods);
        sorted.sort(Comparator.comparing(DateRange::startDate));
        return sorted;
    }

    private boolean isCursorBeyondEnd(LocalDate cursor, LocalDate endDate) {
        return cursor.isAfter(endDate)
                || (TemporalDates.MAX_DATE.equals(cursor) && TemporalDates.MAX_DATE.equals(endDate));
    }
}
