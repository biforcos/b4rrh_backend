package com.b4rrhh.payroll_engine.segment.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkingTimeWindowTest {

    private static final LocalDate D1 = LocalDate.of(2026, 4, 1);
    private static final LocalDate D30 = LocalDate.of(2026, 4, 30);
    private static final BigDecimal FULL = BigDecimal.valueOf(100);
    private static final BigDecimal HALF = BigDecimal.valueOf(50);

    @Test
    void validClosedWindowIsAccepted() {
        assertDoesNotThrow(() -> new WorkingTimeWindow(D1, D30, FULL));
    }

    @Test
    void openEndedWindowIsAccepted() {
        WorkingTimeWindow w = new WorkingTimeWindow(D1, null, HALF);
        assertNull(w.getEndDate());
    }

    @Test
    void startDateNullIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new WorkingTimeWindow(null, D30, FULL));
    }

    @Test
    void workingTimePercentageNullIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new WorkingTimeWindow(D1, D30, null));
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new WorkingTimeWindow(D30, D1, FULL));
    }
}
