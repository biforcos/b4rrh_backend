package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocRequest;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocResult;

/**
 * Port: executes the payroll engine PoC for a single employee and period.
 *
 * <p>The implementation must:
 * <ol>
 *   <li>Build temporal segments from the working time windows in the request.</li>
 *   <li>For each segment, build a {@link com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext}.</li>
 *   <li>Resolve technical values (daysInPeriod, daysInSegment, workingTimePercentage, dailyRate).</li>
 *   <li>Calculate {@code salarioBaseAmount} per segment.</li>
 *   <li>Consolidate {@code totalDevengos} as the sum of all segment amounts.</li>
 * </ol>
 *
 * <p>The same structural plan (concept graph) applies to every segment;
 * only the context changes per segment.
 */
public interface PayrollEnginePocExecutor {

    PayrollEnginePocResult execute(PayrollEnginePocRequest request);
}
