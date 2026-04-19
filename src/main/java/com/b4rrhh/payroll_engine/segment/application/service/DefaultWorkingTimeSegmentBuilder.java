package com.b4rrhh.payroll_engine.segment.application.service;

import com.b4rrhh.payroll_engine.segment.domain.exception.InvalidWorkingTimeCoverageException;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationPeriod;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationSegment;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeSegmentBuilder;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link WorkingTimeSegmentBuilder}.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>Sort windows by {@code startDate}.</li>
 *   <li>For each window, compute the effective interval by intersecting with the period:
 *       <ul>
 *         <li>effective start = max(window.startDate, period.periodStart)</li>
 *         <li>effective end   = min(window.endDate ?? period.periodEnd, period.periodEnd)</li>
 *       </ul>
 *   </li>
 *   <li>Discard windows that fall entirely outside the period.</li>
 *   <li>Validate full, contiguous, non-overlapping coverage of the period (see below).</li>
 *   <li>Produce one {@link CalculationSegment} per effective interval, marking first/last flags.</li>
 * </ol>
 *
 * <h3>Coverage validation</h3>
 * <p>After clipping, the effective intervals must satisfy all of the following:
 * <ul>
 *   <li>At least one effective interval exists.</li>
 *   <li>The first effective interval starts exactly on {@code periodStart}.</li>
 *   <li>Each subsequent effective interval starts exactly on the day after the previous one ends.</li>
 *   <li>The last effective interval ends exactly on {@code periodEnd}.</li>
 * </ul>
 * <p>Gaps and overlaps are rejected with {@link InvalidWorkingTimeCoverageException}.
 * The builder does NOT fill gaps automatically.
 */
@Component
public class DefaultWorkingTimeSegmentBuilder implements WorkingTimeSegmentBuilder {

    @Override
    public List<CalculationSegment> build(CalculationPeriod period, List<WorkingTimeWindow> windows) {
        if (period == null) {
            throw new IllegalArgumentException("period is required");
        }
        if (windows == null || windows.isEmpty()) {
            throw new InvalidWorkingTimeCoverageException(
                    "No working time windows provided for period " +
                    period.getPeriodStart() + " to " + period.getPeriodEnd() +
                    ". At least one window covering the full period is required.");
        }

        List<WorkingTimeWindow> sorted = windows.stream()
                .sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate()))
                .toList();

        // Clip each window to the period and discard those fully outside
        List<EffectiveInterval> effective = new ArrayList<>();
        for (WorkingTimeWindow window : sorted) {
            LocalDate effectiveStart = laterOf(window.getStartDate(), period.getPeriodStart());
            LocalDate declaredEnd = window.getEndDate() != null ? window.getEndDate() : period.getPeriodEnd();
            LocalDate effectiveEnd = earlierOf(declaredEnd, period.getPeriodEnd());

            if (effectiveEnd.isBefore(effectiveStart)) {
                continue; // entirely outside the period
            }
            effective.add(new EffectiveInterval(effectiveStart, effectiveEnd));
        }

        // Rule 1: at least one effective interval must exist
        if (effective.isEmpty()) {
            throw new InvalidWorkingTimeCoverageException(
                    "No working time window intersects period " +
                    period.getPeriodStart() + " to " + period.getPeriodEnd() + ".");
        }

        // Rule 2: first interval must start exactly on periodStart
        LocalDate firstStart = effective.get(0).start;
        if (!firstStart.equals(period.getPeriodStart())) {
            throw new InvalidWorkingTimeCoverageException(
                    "Working time coverage gap at start of period: period starts on " +
                    period.getPeriodStart() + " but first window effective start is " + firstStart + ".");
        }

        // Rule 3: each next interval must start exactly the day after the previous one ends
        for (int i = 1; i < effective.size(); i++) {
            LocalDate previousEnd  = effective.get(i - 1).end;
            LocalDate currentStart = effective.get(i).start;
            LocalDate expectedStart = previousEnd.plusDays(1);
            if (currentStart.isBefore(expectedStart)) {
                throw new InvalidWorkingTimeCoverageException(
                        "Working time windows overlap: window " + (i - 1) + " ends on " + previousEnd +
                        " and window " + i + " starts on " + currentStart +
                        ". Windows must be contiguous and non-overlapping.");
            }
            if (currentStart.isAfter(expectedStart)) {
                throw new InvalidWorkingTimeCoverageException(
                        "Working time coverage gap: window " + (i - 1) + " ends on " + previousEnd +
                        " but window " + i + " starts on " + currentStart +
                        ". Expected start " + expectedStart + ".");
            }
        }

        // Rule 4: last interval must end exactly on periodEnd
        LocalDate lastEnd = effective.get(effective.size() - 1).end;
        if (!lastEnd.equals(period.getPeriodEnd())) {
            throw new InvalidWorkingTimeCoverageException(
                    "Working time coverage gap at end of period: period ends on " +
                    period.getPeriodEnd() + " but last window effective end is " + lastEnd + ".");
        }

        // All validations passed — produce segments
        List<CalculationSegment> result = new ArrayList<>(effective.size());
        for (int i = 0; i < effective.size(); i++) {
            result.add(new CalculationSegment(
                    effective.get(i).start,
                    effective.get(i).end,
                    i == 0,
                    i == effective.size() - 1
            ));
        }
        return result;
    }

    private static LocalDate laterOf(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDate earlierOf(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    /** Local value type to hold a clipped window interval before building segments. */
    private record EffectiveInterval(LocalDate start, LocalDate end) {}
}

