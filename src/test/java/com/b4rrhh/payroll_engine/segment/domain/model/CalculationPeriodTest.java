package com.b4rrhh.payroll_engine.segment.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculationPeriodTest {

    @Test
    void validPeriodIsAccepted() {
        assertDoesNotThrow(() ->
                new CalculationPeriod(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)));
    }

    @Test
    void singleDayPeriodIsAccepted() {
        LocalDate day = LocalDate.of(2026, 4, 15);
        CalculationPeriod p = new CalculationPeriod(day, day);
        assertEquals(day, p.getPeriodStart());
        assertEquals(day, p.getPeriodEnd());
    }

    @Test
    void periodStartNullIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalculationPeriod(null, LocalDate.of(2026, 4, 30)));
    }

    @Test
    void periodEndNullIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalculationPeriod(LocalDate.of(2026, 4, 1), null));
    }

    @Test
    void periodEndBeforeStartIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalculationPeriod(LocalDate.of(2026, 4, 30), LocalDate.of(2026, 4, 1)));
    }
}
