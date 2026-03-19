package com.b4rrhh.employee.temporal.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeTest {

    @Test
    void rejectsInvalidRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DateRange(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 1))
        );
    }

    @Test
    void acceptsOpenEndedRange() {
        DateRange openEnded = new DateRange(LocalDate.of(2026, 1, 1), null);

        assertEquals(TemporalDates.MAX_DATE, openEnded.effectiveEnd(TemporalDates.MAX_DATE));
    }

    @Test
    void containsWorks() {
        DateRange presence = new DateRange(LocalDate.of(2026, 1, 1), null);
        DateRange contract = new DateRange(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 15));

        assertTrue(presence.contains(contract, TemporalDates.MAX_DATE));
    }

    @Test
    void overlapsWorks() {
        DateRange first = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        DateRange second = new DateRange(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31));

        assertTrue(first.overlaps(second, TemporalDates.MAX_DATE));
    }
}
