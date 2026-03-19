package com.b4rrhh.employee.temporal.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelineCoverageValidatorTest {

    private TimelineCoverageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TimelineCoverageValidator();
    }

    @Test
    void fullCoverageSuccess() {
        List<DateRange> subject = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15)),
                new DateRange(LocalDate.of(2026, 1, 16), LocalDate.of(2026, 1, 31))
        );
        List<DateRange> required = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))
        );

        assertTrue(validator.isContained(subject, required));
        assertTrue(validator.isFullyCovered(subject, required));
    }

    @Test
    void gapDetected() {
        List<DateRange> subject = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10)),
                new DateRange(LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 31))
        );
        List<DateRange> required = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))
        );

        assertFalse(validator.isFullyCovered(subject, required));
    }

    @Test
    void outsideRequiredPeriodDetected() {
        List<DateRange> subject = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                new DateRange(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 10))
        );
        List<DateRange> required = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))
        );

        assertFalse(validator.isContained(subject, required));
    }

    @Test
    void openEndedPeriodsHandledCorrectly() {
        List<DateRange> subject = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                new DateRange(LocalDate.of(2026, 2, 1), null)
        );
        List<DateRange> required = List.of(
                new DateRange(LocalDate.of(2026, 1, 1), null)
        );

        assertTrue(validator.isContained(subject, required));
        assertTrue(validator.isFullyCovered(subject, required));
    }
}
