package com.b4rrhh.payroll_engine.segment.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculationSegmentTest {

    private static final LocalDate D1 = LocalDate.of(2026, 4, 1);
    private static final LocalDate D14 = LocalDate.of(2026, 4, 14);

    @Test
    void validSegmentIsAccepted() {
        assertDoesNotThrow(() -> new CalculationSegment(D1, D14, true, false));
    }

    @Test
    void singleDaySegmentHasLengthOne() {
        CalculationSegment seg = new CalculationSegment(D1, D1, true, true);
        assertEquals(1L, seg.lengthInDaysInclusive());
    }

    @Test
    void lengthInDaysInclusiveIsCorrect() {
        CalculationSegment seg = new CalculationSegment(D1, D14, true, false);
        assertEquals(14L, seg.lengthInDaysInclusive());
    }

    @Test
    void segmentStartNullIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalculationSegment(null, D14, true, false));
    }

    @Test
    void segmentEndNullIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalculationSegment(D1, null, true, false));
    }

    @Test
    void segmentEndBeforeStartIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalculationSegment(D14, D1, true, false));
    }
}
