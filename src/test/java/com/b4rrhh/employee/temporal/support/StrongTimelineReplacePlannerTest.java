package com.b4rrhh.employee.temporal.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StrongTimelineReplacePlannerTest {

    private StrongTimelineReplacePlanner planner;

    @BeforeEach
    void setUp() {
        planner = new StrongTimelineReplacePlanner();
    }

    @Test
    void exactOpenEndedPeriodUsesExactStartMode() {
        StrongTimelineReplacePlan plan = planner.plan(
                List.of(range(LocalDate.of(2026, 1, 1), null)),
                LocalDate.of(2026, 1, 1)
        );

        assertEquals(ReplaceMode.EXACT_START, plan.mode());
        assertEquals(0, plan.coveringPeriodIndex());
        assertRange(plan.periodToUpdate(), LocalDate.of(2026, 1, 1), null);
        assertNull(plan.periodToInsert());
        assertEquals(plan.periodToUpdate(), plan.resultPeriod());
    }

    @Test
    void exactClosedPeriodUsesExactStartMode() {
        StrongTimelineReplacePlan plan = planner.plan(
                List.of(range(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))),
                LocalDate.of(2026, 1, 1)
        );

        assertEquals(ReplaceMode.EXACT_START, plan.mode());
        assertEquals(0, plan.coveringPeriodIndex());
        assertRange(plan.periodToUpdate(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        assertNull(plan.periodToInsert());
        assertEquals(plan.periodToUpdate(), plan.resultPeriod());
    }

    @Test
    void splitOpenEndedPeriodUsesSplitMode() {
        StrongTimelineReplacePlan plan = planner.plan(
                List.of(range(LocalDate.of(2026, 1, 1), null)),
                LocalDate.of(2026, 3, 1)
        );

        assertEquals(ReplaceMode.SPLIT, plan.mode());
        assertEquals(0, plan.coveringPeriodIndex());
        assertRange(plan.periodToUpdate(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
        assertRange(plan.periodToInsert(), LocalDate.of(2026, 3, 1), null);
        assertEquals(plan.periodToInsert(), plan.resultPeriod());
    }

    @Test
    void splitClosedPeriodUsesSplitMode() {
        StrongTimelineReplacePlan plan = planner.plan(
                List.of(range(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))),
                LocalDate.of(2026, 3, 1)
        );

        assertEquals(ReplaceMode.SPLIT, plan.mode());
        assertEquals(0, plan.coveringPeriodIndex());
        assertRange(plan.periodToUpdate(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
        assertRange(plan.periodToInsert(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
        assertEquals(plan.periodToInsert(), plan.resultPeriod());
    }

    @Test
    void noCoveringPeriodCreatesOpenEndedInsert() {
        StrongTimelineReplacePlan plan = planner.plan(
                List.of(range(LocalDate.of(2026, 4, 1), null)),
                LocalDate.of(2026, 3, 1)
        );

        assertEquals(ReplaceMode.NO_COVERING, plan.mode());
        assertNull(plan.coveringPeriodIndex());
        assertNull(plan.periodToUpdate());
        assertRange(plan.periodToInsert(), LocalDate.of(2026, 3, 1), null);
        assertEquals(plan.periodToInsert(), plan.resultPeriod());
    }

    @Test
    void boundaryAtCoveringEndCreatesOneDayReplacement() {
        StrongTimelineReplacePlan plan = planner.plan(
                List.of(range(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))),
                LocalDate.of(2026, 3, 31)
        );

        assertEquals(ReplaceMode.SPLIT, plan.mode());
        assertEquals(0, plan.coveringPeriodIndex());
        assertRange(plan.periodToUpdate(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 30));
        assertRange(plan.periodToInsert(), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 3, 31));
    }

    @Test
    void boundaryAtNextPeriodStartUsesExactStartOnSecondPeriod() {
        StrongTimelineReplacePlan plan = planner.plan(
                List.of(
                        range(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                        range(LocalDate.of(2026, 2, 1), null)
                ),
                LocalDate.of(2026, 2, 1)
        );

        assertEquals(ReplaceMode.EXACT_START, plan.mode());
        assertEquals(1, plan.coveringPeriodIndex());
        assertRange(plan.periodToUpdate(), LocalDate.of(2026, 2, 1), null);
        assertNull(plan.periodToInsert());
    }

    private DateRange range(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    private void assertRange(DateRange range, LocalDate expectedStartDate, LocalDate expectedEndDate) {
        assertEquals(expectedStartDate, range.startDate());
        assertEquals(expectedEndDate, range.endDate());
    }
}
