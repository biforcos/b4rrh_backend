package com.b4rrhh.employee.temporal.support;

import java.time.LocalDate;
import java.util.List;

public final class StrongTimelineReplacePlanner {

    public StrongTimelineReplacePlan plan(List<DateRange> currentHistory, LocalDate effectiveDate) {
        if (currentHistory == null) {
            throw new IllegalArgumentException("currentHistory is required");
        }
        if (effectiveDate == null) {
            throw new IllegalArgumentException("effectiveDate is required");
        }

        for (int index = 0; index < currentHistory.size(); index++) {
            DateRange period = currentHistory.get(index);
            if (period == null) {
                throw new IllegalArgumentException("currentHistory contains null period");
            }

            if (!isCoveringDate(period, effectiveDate)) {
                continue;
            }

            if (period.startDate().equals(effectiveDate)) {
                DateRange replaced = new DateRange(period.startDate(), period.endDate());
                return new StrongTimelineReplacePlan(
                        ReplaceMode.EXACT_START,
                        index,
                        replaced,
                        null,
                        replaced
                );
            }

            DateRange adjustedExisting = new DateRange(
                    period.startDate(),
                    TemporalDates.previousDay(effectiveDate)
            );
            DateRange replacement = new DateRange(effectiveDate, period.endDate());

            return new StrongTimelineReplacePlan(
                    ReplaceMode.SPLIT,
                    index,
                    adjustedExisting,
                    replacement,
                    replacement
            );
        }

        DateRange replacement = new DateRange(effectiveDate, null);
        return new StrongTimelineReplacePlan(
                ReplaceMode.NO_COVERING,
                null,
                null,
                replacement,
                replacement
        );
    }

    private boolean isCoveringDate(DateRange period, LocalDate date) {
        if (period.startDate().isAfter(date)) {
            return false;
        }

        return !period.effectiveEnd(TemporalDates.MAX_DATE).isBefore(date);
    }
}
