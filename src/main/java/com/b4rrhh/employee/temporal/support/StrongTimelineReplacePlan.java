package com.b4rrhh.employee.temporal.support;

public record StrongTimelineReplacePlan(
        ReplaceMode mode,
        Integer coveringPeriodIndex,
        DateRange periodToUpdate,
        DateRange periodToInsert,
        DateRange resultPeriod
) {

    public StrongTimelineReplacePlan {
        if (mode == null) {
            throw new IllegalArgumentException("mode is required");
        }
        if (resultPeriod == null) {
            throw new IllegalArgumentException("resultPeriod is required");
        }

        switch (mode) {
            case NO_COVERING -> validateNoCovering(coveringPeriodIndex, periodToUpdate, periodToInsert, resultPeriod);
            case EXACT_START -> validateExactStart(coveringPeriodIndex, periodToUpdate, periodToInsert, resultPeriod);
            case SPLIT -> validateSplit(coveringPeriodIndex, periodToUpdate, periodToInsert, resultPeriod);
            default -> throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
    }

    public boolean hasCoveringPeriod() {
        return coveringPeriodIndex != null;
    }

    public boolean hasPeriodToUpdate() {
        return periodToUpdate != null;
    }

    public boolean hasPeriodToInsert() {
        return periodToInsert != null;
    }

    private static void validateNoCovering(
            Integer coveringPeriodIndex,
            DateRange periodToUpdate,
            DateRange periodToInsert,
            DateRange resultPeriod
    ) {
        if (coveringPeriodIndex != null) {
            throw new IllegalArgumentException("coveringPeriodIndex must be null for NO_COVERING mode");
        }
        if (periodToUpdate != null) {
            throw new IllegalArgumentException("periodToUpdate must be null for NO_COVERING mode");
        }
        if (periodToInsert == null) {
            throw new IllegalArgumentException("periodToInsert is required for NO_COVERING mode");
        }
        if (!resultPeriod.equals(periodToInsert)) {
            throw new IllegalArgumentException("resultPeriod must match periodToInsert for NO_COVERING mode");
        }
    }

    private static void validateExactStart(
            Integer coveringPeriodIndex,
            DateRange periodToUpdate,
            DateRange periodToInsert,
            DateRange resultPeriod
    ) {
        if (coveringPeriodIndex == null || coveringPeriodIndex < 0) {
            throw new IllegalArgumentException("coveringPeriodIndex is required for EXACT_START mode");
        }
        if (periodToUpdate == null) {
            throw new IllegalArgumentException("periodToUpdate is required for EXACT_START mode");
        }
        if (periodToInsert != null) {
            throw new IllegalArgumentException("periodToInsert must be null for EXACT_START mode");
        }
        if (!resultPeriod.equals(periodToUpdate)) {
            throw new IllegalArgumentException("resultPeriod must match periodToUpdate for EXACT_START mode");
        }
    }

    private static void validateSplit(
            Integer coveringPeriodIndex,
            DateRange periodToUpdate,
            DateRange periodToInsert,
            DateRange resultPeriod
    ) {
        if (coveringPeriodIndex == null || coveringPeriodIndex < 0) {
            throw new IllegalArgumentException("coveringPeriodIndex is required for SPLIT mode");
        }
        if (periodToUpdate == null) {
            throw new IllegalArgumentException("periodToUpdate is required for SPLIT mode");
        }
        if (periodToInsert == null) {
            throw new IllegalArgumentException("periodToInsert is required for SPLIT mode");
        }
        if (!resultPeriod.equals(periodToInsert)) {
            throw new IllegalArgumentException("resultPeriod must match periodToInsert for SPLIT mode");
        }
    }
}
