package com.b4rrhh.payroll_engine.segment.domain.model;

import java.util.List;

/**
 * Port: builds an ordered, contiguous, non-overlapping, exhaustive list of
 * {@link CalculationSegment} that covers the given {@link CalculationPeriod}.
 *
 * <p>In this iteration, only changes in working time percentage create segment boundaries.
 * Each {@link WorkingTimeWindow} in the supplied list defines a stretch where working time
 * is constant; the builder clips and intersects those windows against the period.
 *
 * <p>Implementations must guarantee:
 * <ul>
 *   <li>segments are ordered by start date</li>
 *   <li>segments are contiguous (no gaps)</li>
 *   <li>segments are non-overlapping</li>
 *   <li>collectively they cover exactly the period from {@code periodStart} to {@code periodEnd}</li>
 *   <li>exactly one segment has {@code firstSegment=true} and exactly one has {@code lastSegment=true}</li>
 * </ul>
 *
 * <p>For this PoC the working time windows are assumed to be coherent (no gaps, no overlaps)
 * and to cover the full period. Validation of window coherence is not in scope.
 */
public interface WorkingTimeSegmentBuilder {

    /**
     * @param period  the full payroll period to segment
     * @param windows ordered, coherent working-time windows (no gaps assumed for this PoC)
     * @return ordered list of segments covering the period
     */
    List<CalculationSegment> build(CalculationPeriod period, List<WorkingTimeWindow> windows);
}
