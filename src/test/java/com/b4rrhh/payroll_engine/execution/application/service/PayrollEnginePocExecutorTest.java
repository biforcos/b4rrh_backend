package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocRequest;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionResult;
import com.b4rrhh.payroll_engine.segment.application.service.DefaultWorkingTimeSegmentBuilder;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollEnginePocExecutorTest {

    private final DefaultPayrollEnginePocExecutor executor =
            new DefaultPayrollEnginePocExecutor(
                    new DefaultWorkingTimeSegmentBuilder(),
                    new DefaultExecutionPlanBuilder(),
                    new DefaultSegmentExecutionEngine(new SegmentTechnicalValueResolver()));

    private static final LocalDate APR_01 = LocalDate.of(2026, 4, 1);
    private static final LocalDate APR_14 = LocalDate.of(2026, 4, 14);
    private static final LocalDate APR_15 = LocalDate.of(2026, 4, 15);
    private static final LocalDate APR_30 = LocalDate.of(2026, 4, 30);

    private PayrollEnginePocRequest referenceRequest() {
        return new PayrollEnginePocRequest(
                "ESP",
                "EMP",
                "EMP0001",
                APR_01,
                APR_30,
                new BigDecimal("2000.00"),
                List.of(
                        new WorkingTimeWindow(APR_01, APR_14, new BigDecimal("100")),
                        new WorkingTimeWindow(APR_15, null,   new BigDecimal("50"))
                )
        );
    }

    // ── main PoC scenario ────────────────────────────────────────────────────

    @Test
    void executionProducesTwoSegments() {
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        assertEquals(2, result.getSegmentResults().size());
    }

    @Test
    void segmentOneDatesAndFlags() {
        SegmentExecutionResult seg = executor.execute(referenceRequest()).getSegmentResults().get(0);

        assertEquals(APR_01, seg.getSegmentStart());
        assertEquals(APR_14, seg.getSegmentEnd());
        assertTrue(seg.isFirstSegment());
        assertFalse(seg.isLastSegment());
        assertEquals(14L, seg.getDaysInSegment());
    }

    @Test
    void segmentTwoDatesAndFlags() {
        SegmentExecutionResult seg = executor.execute(referenceRequest()).getSegmentResults().get(1);

        assertEquals(APR_15, seg.getSegmentStart());
        assertEquals(APR_30, seg.getSegmentEnd());
        assertFalse(seg.isFirstSegment());
        assertTrue(seg.isLastSegment());
        assertEquals(16L, seg.getDaysInSegment());
    }

    @Test
    void daysInPeriodIsThirtyInBothSegments() {
        List<SegmentExecutionResult> segments = executor.execute(referenceRequest()).getSegmentResults();
        assertEquals(30L, segments.get(0).getDaysInPeriod());
        assertEquals(30L, segments.get(1).getDaysInPeriod());
    }

    @Test
    void dailyRateOfSegmentTwoIsLowerThanSegmentOne() {
        List<SegmentExecutionResult> segments = executor.execute(referenceRequest()).getSegmentResults();
        assertTrue(segments.get(1).getDailyRate().compareTo(segments.get(0).getDailyRate()) < 0,
                "segment 2 dailyRate (50%) must be lower than segment 1 (100%)");
    }

    @Test
    void salarioBaseAmountOfSegmentTwoIsLowerThanSegmentOne() {
        List<SegmentExecutionResult> segments = executor.execute(referenceRequest()).getSegmentResults();
        assertTrue(
                segments.get(1).getSalarioBaseAmount().compareTo(segments.get(0).getSalarioBaseAmount()) < 0,
                "segment 2 salarioBase must be lower than segment 1");
    }

    @Test
    void totalDevengosEqualsSumOfBothSegments() {
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        BigDecimal expected = result.getSegmentResults().stream()
                .map(SegmentExecutionResult::getSalarioBaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, expected.compareTo(result.getTotalDevengos()),
                "totalDevengos must equal the sum of segment salarioBaseAmounts");
    }

    // ── validation tests ─────────────────────────────────────────────────────

    @Test
    void requestWithNullMonthlySalaryIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new PayrollEnginePocRequest(
                        "ESP", "EMP", "EMP0001", APR_01, APR_30,
                        null,
                        List.of(new WorkingTimeWindow(APR_01, null, new BigDecimal("100")))
                )
        );
    }

    @Test
    void requestWithNegativeMonthlySalaryIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new PayrollEnginePocRequest(
                        "ESP", "EMP", "EMP0001", APR_01, APR_30,
                        new BigDecimal("-1"),
                        List.of(new WorkingTimeWindow(APR_01, null, new BigDecimal("100")))
                )
        );
    }

    @Test
    void concreteAmountsAreCorrectForReferenceCase() {
        // April 2026: 30 days, 2000€/month
        // Segment 1: 14 days at 100% → dailyRate = 2000/30 * 1 = 66.66666667
        //   salarioBase = 14 * 66.66666667 = 933.33 (scale 2 HALF_UP)
        // Segment 2: 16 days at 50%  → dailyRate = 2000/30 * 0.5 = 33.33333334
        //   salarioBase = 16 * 33.33333334 = 533.33 (scale 2 HALF_UP)
        // totalDevengos = 933.33 + 533.33 = 1466.66
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        BigDecimal seg1Amount = result.getSegmentResults().get(0).getSalarioBaseAmount();
        BigDecimal seg2Amount = result.getSegmentResults().get(1).getSalarioBaseAmount();

        assertEquals(0, new BigDecimal("933.33").compareTo(seg1Amount),
                "segment 1 salarioBase");
        assertEquals(0, new BigDecimal("533.33").compareTo(seg2Amount),
                "segment 2 salarioBase");
        assertEquals(0, new BigDecimal("1466.66").compareTo(result.getTotalDevengos()),
                "totalDevengos");
    }

    @Test
    void zeroSalaryProducesZeroAmounts() {
        PayrollEnginePocRequest zeroRequest = new PayrollEnginePocRequest(
                "ESP", "EMP", "EMP0001", APR_01, APR_30,
                BigDecimal.ZERO,
                List.of(new WorkingTimeWindow(APR_01, null, new BigDecimal("100")))
        );
        PayrollEnginePocResult result = executor.execute(zeroRequest);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalDevengos()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getSegmentResults().get(0).getSalarioBaseAmount()));
    }
}
