package com.b4rrhh.payroll_engine.segment.application.service;

import com.b4rrhh.payroll_engine.segment.domain.model.CalculationPeriod;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationSegment;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.b4rrhh.payroll_engine.segment.domain.exception.InvalidWorkingTimeCoverageException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkingTimeSegmentBuilderTest {

    private final DefaultWorkingTimeSegmentBuilder builder = new DefaultWorkingTimeSegmentBuilder();

    private static final LocalDate APR_01 = LocalDate.of(2026, 4, 1);
    private static final LocalDate APR_14 = LocalDate.of(2026, 4, 14);
    private static final LocalDate APR_15 = LocalDate.of(2026, 4, 15);
    private static final LocalDate APR_30 = LocalDate.of(2026, 4, 30);

    private static final BigDecimal FULL = BigDecimal.valueOf(100);
    private static final BigDecimal HALF = BigDecimal.valueOf(50);

    // ── helpers ──────────────────────────────────────────────────────────────

    private CalculationPeriod aprilPeriod() {
        return new CalculationPeriod(APR_01, APR_30);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void emptyWindowListIsRejected() {
        assertThrows(InvalidWorkingTimeCoverageException.class,
                () -> builder.build(aprilPeriod(), List.of()));
    }

    // ── coverage validation tests ─────────────────────────────────────────────

    @Test
    void gapAtStartIsRejected() {
        // window starts on APR_10 — days 1..9 are uncovered
        WorkingTimeWindow w = new WorkingTimeWindow(LocalDate.of(2026, 4, 10), APR_30, FULL);
        assertThrows(InvalidWorkingTimeCoverageException.class,
                () -> builder.build(aprilPeriod(), List.of(w)));
    }

    @Test
    void gapInMiddleIsRejected() {
        // w1 ends APR_14, w2 starts APR_16 — APR_15 is uncovered
        WorkingTimeWindow w1 = new WorkingTimeWindow(APR_01, APR_14, FULL);
        WorkingTimeWindow w2 = new WorkingTimeWindow(LocalDate.of(2026, 4, 16), APR_30, HALF);
        assertThrows(InvalidWorkingTimeCoverageException.class,
                () -> builder.build(aprilPeriod(), List.of(w1, w2)));
    }

    @Test
    void gapAtEndIsRejected() {
        // window ends APR_28 — days 29..30 are uncovered
        WorkingTimeWindow w = new WorkingTimeWindow(APR_01, LocalDate.of(2026, 4, 28), FULL);
        assertThrows(InvalidWorkingTimeCoverageException.class,
                () -> builder.build(aprilPeriod(), List.of(w)));
    }

    @Test
    void overlapIsRejected() {
        // w1 ends APR_15, w2 starts APR_15 — overlap on APR_15
        WorkingTimeWindow w1 = new WorkingTimeWindow(APR_01, APR_15, FULL);
        WorkingTimeWindow w2 = new WorkingTimeWindow(APR_15, APR_30, HALF);
        assertThrows(InvalidWorkingTimeCoverageException.class,
                () -> builder.build(aprilPeriod(), List.of(w1, w2)));
    }

    @Test
    void singleWindowCoveringFullPeriodProducesOneSegment() {
        WorkingTimeWindow window = new WorkingTimeWindow(APR_01, APR_30, FULL);

        List<CalculationSegment> segments = builder.build(aprilPeriod(), List.of(window));

        assertEquals(1, segments.size());
        CalculationSegment seg = segments.get(0);
        assertEquals(APR_01, seg.getSegmentStart());
        assertEquals(APR_30, seg.getSegmentEnd());
        assertTrue(seg.isFirstSegment());
        assertTrue(seg.isLastSegment());
    }

    @Test
    void twoWindowsProduceTwoContiguousSegments() {
        // reference case from specification
        WorkingTimeWindow w1 = new WorkingTimeWindow(APR_01, APR_14, FULL);
        WorkingTimeWindow w2 = new WorkingTimeWindow(APR_15, null, HALF);

        List<CalculationSegment> segments = builder.build(aprilPeriod(), List.of(w1, w2));

        assertEquals(2, segments.size());

        CalculationSegment seg1 = segments.get(0);
        assertEquals(APR_01, seg1.getSegmentStart());
        assertEquals(APR_14, seg1.getSegmentEnd());
        assertTrue(seg1.isFirstSegment());
        assertFalse(seg1.isLastSegment());

        CalculationSegment seg2 = segments.get(1);
        assertEquals(APR_15, seg2.getSegmentStart());
        assertEquals(APR_30, seg2.getSegmentEnd());
        assertFalse(seg2.isFirstSegment());
        assertTrue(seg2.isLastSegment());
    }

    @Test
    void firstAndLastFlagsAreCorrectWithMultipleSegments() {
        WorkingTimeWindow w1 = new WorkingTimeWindow(APR_01, APR_14, FULL);
        WorkingTimeWindow w2 = new WorkingTimeWindow(APR_15, null, HALF);

        List<CalculationSegment> segments = builder.build(aprilPeriod(), List.of(w1, w2));

        long firstCount = segments.stream().filter(CalculationSegment::isFirstSegment).count();
        long lastCount  = segments.stream().filter(CalculationSegment::isLastSegment).count();

        assertEquals(1, firstCount, "exactly one segment must be first");
        assertEquals(1, lastCount,  "exactly one segment must be last");
        assertTrue(segments.get(0).isFirstSegment(), "index 0 must be first");
        assertTrue(segments.get(segments.size() - 1).isLastSegment(), "last index must be last");
    }

    @Test
    void segmentsAreContiguousAndCoverFullPeriod() {
        WorkingTimeWindow w1 = new WorkingTimeWindow(APR_01, APR_14, FULL);
        WorkingTimeWindow w2 = new WorkingTimeWindow(APR_15, null, HALF);

        List<CalculationSegment> segments = builder.build(aprilPeriod(), List.of(w1, w2));

        // first segment starts on period start
        assertEquals(APR_01, segments.get(0).getSegmentStart());
        // last segment ends on period end
        assertEquals(APR_30, segments.get(segments.size() - 1).getSegmentEnd());

        // each segment starts the day after the previous one ends (no gaps)
        for (int i = 1; i < segments.size(); i++) {
            LocalDate previousEnd   = segments.get(i - 1).getSegmentEnd();
            LocalDate currentStart  = segments.get(i).getSegmentStart();
            assertEquals(previousEnd.plusDays(1), currentStart,
                    "segment " + i + " must start exactly the day after segment " + (i - 1) + " ends");
        }
    }

    @Test
    void segmentDaysAreCorrectForReferenceCase() {
        WorkingTimeWindow w1 = new WorkingTimeWindow(APR_01, APR_14, FULL);
        WorkingTimeWindow w2 = new WorkingTimeWindow(APR_15, null, HALF);

        List<CalculationSegment> segments = builder.build(aprilPeriod(), List.of(w1, w2));

        assertEquals(14L, segments.get(0).lengthInDaysInclusive(), "first segment: 1-14 April = 14 days");
        assertEquals(16L, segments.get(1).lengthInDaysInclusive(), "second segment: 15-30 April = 16 days");
    }

    @Test
    void openEndedWindowIsClippedToPeriodEnd() {
        WorkingTimeWindow openWindow = new WorkingTimeWindow(APR_01, null, FULL);

        List<CalculationSegment> segments = builder.build(aprilPeriod(), List.of(openWindow));

        assertEquals(1, segments.size());
        assertEquals(APR_30, segments.get(0).getSegmentEnd());
    }
}
