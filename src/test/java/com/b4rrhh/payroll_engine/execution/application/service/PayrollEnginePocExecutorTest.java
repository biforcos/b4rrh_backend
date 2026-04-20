package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.ExecutionScope;
import com.b4rrhh.payroll_engine.concept.domain.model.FeedMode;
import com.b4rrhh.payroll_engine.concept.domain.model.FunctionalNature;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.model.ResultCompositionMode;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptFeedRelationRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import com.b4rrhh.payroll_engine.dependency.application.service.DefaultConceptDependencyGraphService;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingPocConceptException;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocRequest;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionResult;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.segment.application.service.DefaultWorkingTimeSegmentBuilder;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollEnginePocExecutorTest {

    private final DefaultPayrollEnginePocExecutor executor =
            new DefaultPayrollEnginePocExecutor(
                    new DefaultWorkingTimeSegmentBuilder(),
                    stubConceptRepository("ESP"),
                    new DefaultConceptDependencyGraphService(pocFeedRelationRepo("ESP")),
                    new DefaultExecutionPlanBuilder(
                            pocOperandRepo("ESP"),
                            new RateByQuantityConfigurationValidator()),
                    new DefaultSegmentExecutionEngine(
                            new SegmentTechnicalValueResolver(),
                            new RateByQuantityOperandResolver()));

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
    void totalSalarioBaseEqualsSumOfBothSegments() {
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        BigDecimal expected = result.getSegmentResults().stream()
                .map(SegmentExecutionResult::getSalarioBaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, expected.compareTo(result.getTotalSalarioBase()),
                "totalSalarioBase must equal the sum of segment salarioBaseAmounts");
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
        // totalSalarioBase = 933.33 + 533.33 = 1466.66
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        BigDecimal seg1Amount = result.getSegmentResults().get(0).getSalarioBaseAmount();
        BigDecimal seg2Amount = result.getSegmentResults().get(1).getSalarioBaseAmount();

        assertEquals(0, new BigDecimal("933.33").compareTo(seg1Amount),
                "segment 1 salarioBase");
        assertEquals(0, new BigDecimal("533.33").compareTo(seg2Amount),
                "segment 2 salarioBase");
        assertEquals(0, new BigDecimal("1466.66").compareTo(result.getTotalSalarioBase()),
                "totalSalarioBase");
    }

    @Test
    void zeroSalaryProducesZeroAmounts() {
        PayrollEnginePocRequest zeroRequest = new PayrollEnginePocRequest(
                "ESP", "EMP", "EMP0001", APR_01, APR_30,
                BigDecimal.ZERO,
                List.of(new WorkingTimeWindow(APR_01, null, new BigDecimal("100")))
        );
        PayrollEnginePocResult result = executor.execute(zeroRequest);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalSalarioBase()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getSegmentResults().get(0).getSalarioBaseAmount()));
    }

    @Test
    void missingPocConceptThrowsMissingPocConceptException() {
        DefaultPayrollEnginePocExecutor executorWithEmptyRepo =
                new DefaultPayrollEnginePocExecutor(
                        new DefaultWorkingTimeSegmentBuilder(),
                        emptyConceptRepository(),
                        new DefaultConceptDependencyGraphService(emptyFeedRelationRepo()),
                        new DefaultExecutionPlanBuilder(
                                new PayrollConceptOperandRepository() {
                                    @Override
                                    public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
                                    @Override
                                    public List<PayrollConceptOperand> findByTarget(String rs, String code) { return Collections.emptyList(); }
                                },
                                new RateByQuantityConfigurationValidator()),
                        new DefaultSegmentExecutionEngine(
                                new SegmentTechnicalValueResolver(),
                                new RateByQuantityOperandResolver()));
        assertThrows(MissingPocConceptException.class, () ->
                executorWithEmptyRepo.execute(referenceRequest()));
    }

    // ── repository stubs ─────────────────────────────────────────────────────

    /**
     * Returns a stub concept repository seeded with the 6 PoC concepts for the given rule system.
     * Concepts are assigned sequential IDs (1–6) so that feed relation lookup by ID works.
     */
    private static PayrollConceptRepository stubConceptRepository(String ruleSystemCode) {
        PayrollConcept diasPresencia = pocConcept(
                1L, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO",
                CalculationType.DIRECT_AMOUNT, FunctionalNature.INFORMATIONAL);
        PayrollConcept precioDia = pocConcept(
                2L, ruleSystemCode, "T_PRECIO_DIA",
                CalculationType.DIRECT_AMOUNT, FunctionalNature.INFORMATIONAL);
        PayrollConcept salarioBase = pocConcept(
                3L, ruleSystemCode, "SALARIO_BASE",
                CalculationType.RATE_BY_QUANTITY, FunctionalNature.EARNING);
        PayrollConcept precioTransporte = pocConcept(
                4L, ruleSystemCode, "T_PRECIO_TRANSPORTE",
                CalculationType.DIRECT_AMOUNT, FunctionalNature.INFORMATIONAL);
        PayrollConcept plusTransporte = pocConcept(
                5L, ruleSystemCode, "PLUS_TRANSPORTE",
                CalculationType.RATE_BY_QUANTITY, FunctionalNature.EARNING);
        PayrollConcept totalDevengosSegmento = pocConcept(
                6L, ruleSystemCode, "TOTAL_DEVENGOS_SEGMENTO",
                CalculationType.AGGREGATE, FunctionalNature.EARNING);

        Map<String, PayrollConcept> index = new HashMap<>();
        index.put("T_DIAS_PRESENCIA_SEGMENTO", diasPresencia);
        index.put("T_PRECIO_DIA", precioDia);
        index.put("SALARIO_BASE", salarioBase);
        index.put("T_PRECIO_TRANSPORTE", precioTransporte);
        index.put("PLUS_TRANSPORTE", plusTransporte);
        index.put("TOTAL_DEVENGOS_SEGMENTO", totalDevengosSegmento);

        return new PayrollConceptRepository() {
            @Override
            public PayrollConcept save(PayrollConcept c) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Optional<PayrollConcept> findByBusinessKey(String rs, String code) {
                if (!ruleSystemCode.equals(rs)) return Optional.empty();
                return Optional.ofNullable(index.get(code));
            }
            @Override
            public boolean existsByBusinessKey(String rs, String code) {
                return ruleSystemCode.equals(rs) && index.containsKey(code);
            }
        };
    }

    /**
     * Returns a stub feed relation repository reflecting the full PoC structure:
     * <ul>
     *   <li>SALARIO_BASE (id=3) is fed by T_DIAS_PRESENCIA_SEGMENTO (id=1) and T_PRECIO_DIA (id=2).</li>
     *   <li>PLUS_TRANSPORTE (id=5) is fed by T_DIAS_PRESENCIA_SEGMENTO (id=1) and T_PRECIO_TRANSPORTE (id=4).</li>
     *   <li>TOTAL_DEVENGOS_SEGMENTO (id=6) is fed by SALARIO_BASE (id=3) and PLUS_TRANSPORTE (id=5).</li>
     * </ul>
     */
    private static PayrollConceptFeedRelationRepository pocFeedRelationRepo(String ruleSystemCode) {
        PayrollObject diasPresenciaObj    = pocObject(1L, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject precioDiaObj        = pocObject(2L, ruleSystemCode, "T_PRECIO_DIA");
        PayrollObject salarioBaseObj      = pocObject(3L, ruleSystemCode, "SALARIO_BASE");
        PayrollObject precioTransporteObj = pocObject(4L, ruleSystemCode, "T_PRECIO_TRANSPORTE");
        PayrollObject plusTransporteObj   = pocObject(5L, ruleSystemCode, "PLUS_TRANSPORTE");
        PayrollObject totalDevengosObj    = pocObject(6L, ruleSystemCode, "TOTAL_DEVENGOS_SEGMENTO");

        List<PayrollConceptFeedRelation> salarioBaseFeeds = List.of(
                feedRelation(diasPresenciaObj, salarioBaseObj),
                feedRelation(precioDiaObj, salarioBaseObj)
        );
        List<PayrollConceptFeedRelation> plusTransporteFeeds = List.of(
                feedRelation(diasPresenciaObj, plusTransporteObj),
                feedRelation(precioTransporteObj, plusTransporteObj)
        );
        List<PayrollConceptFeedRelation> totalDevengosFeeds = List.of(
                feedRelation(salarioBaseObj, totalDevengosObj),
                feedRelation(plusTransporteObj, totalDevengosObj)
        );

        Map<Long, List<PayrollConceptFeedRelation>> relsByTarget = new HashMap<>();
        relsByTarget.put(3L, salarioBaseFeeds);
        relsByTarget.put(5L, plusTransporteFeeds);
        relsByTarget.put(6L, totalDevengosFeeds);

        return new PayrollConceptFeedRelationRepository() {
            @Override
            public PayrollConceptFeedRelation save(PayrollConceptFeedRelation r) {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<PayrollConceptFeedRelation> findActiveByTargetObjectId(Long id, LocalDate date) {
                return relsByTarget.getOrDefault(id, Collections.emptyList());
            }
        };
    }

    private static PayrollConceptFeedRelationRepository emptyFeedRelationRepo() {
        return new PayrollConceptFeedRelationRepository() {
            @Override
            public PayrollConceptFeedRelation save(PayrollConceptFeedRelation r) {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<PayrollConceptFeedRelation> findActiveByTargetObjectId(Long id, LocalDate date) {
                return Collections.emptyList();
            }
        };
    }

    private static PayrollConceptRepository emptyConceptRepository() {
        return new PayrollConceptRepository() {
            @Override
            public PayrollConcept save(PayrollConcept c) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Optional<PayrollConcept> findByBusinessKey(String rs, String code) {
                return Optional.empty();
            }
            @Override
            public boolean existsByBusinessKey(String rs, String code) {
                return false;
            }
        };
    }

    private static PayrollConcept pocConcept(
            Long id,
            String ruleSystemCode,
            String conceptCode,
            CalculationType calculationType,
            FunctionalNature nature
    ) {
        PayrollObject object = new PayrollObject(
                id, ruleSystemCode, PayrollObjectTypeCode.CONCEPT, conceptCode, null, null);
        return new PayrollConcept(
                object,
                conceptCode,
                calculationType,
                nature,
                ResultCompositionMode.REPLACE,
                null,
                ExecutionScope.SEGMENT,
                null, null
        );
    }

    private static PayrollObject pocObject(Long id, String ruleSystemCode, String conceptCode) {
        return new PayrollObject(id, ruleSystemCode, PayrollObjectTypeCode.CONCEPT, conceptCode, null, null);
    }

    private static PayrollConceptFeedRelation feedRelation(PayrollObject source, PayrollObject target) {
        return new PayrollConceptFeedRelation(
                null, source, target, FeedMode.FEED_BY_SOURCE, null,
                LocalDate.of(2020, 1, 1), null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    /**
     * Returns a stub operand repository for all PoC RATE_BY_QUANTITY concepts:
     * <ul>
     *   <li>SALARIO_BASE (id=3): QUANTITY=T_DIAS_PRESENCIA_SEGMENTO (id=1), RATE=T_PRECIO_DIA (id=2).</li>
     *   <li>PLUS_TRANSPORTE (id=5): QUANTITY=T_DIAS_PRESENCIA_SEGMENTO (id=1), RATE=T_PRECIO_TRANSPORTE (id=4).</li>
     * </ul>
     */
    private static PayrollConceptOperandRepository pocOperandRepo(String ruleSystemCode) {
        PayrollObject diasPresenciaObj    = pocObject(1L, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject precioDiaObj        = pocObject(2L, ruleSystemCode, "T_PRECIO_DIA");
        PayrollObject salarioBaseObj      = pocObject(3L, ruleSystemCode, "SALARIO_BASE");
        PayrollObject precioTransporteObj = pocObject(4L, ruleSystemCode, "T_PRECIO_TRANSPORTE");
        PayrollObject plusTransporteObj   = pocObject(5L, ruleSystemCode, "PLUS_TRANSPORTE");

        List<PayrollConceptOperand> salarioBaseOperands = List.of(
                new PayrollConceptOperand(null, salarioBaseObj, OperandRole.QUANTITY, diasPresenciaObj,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, salarioBaseObj, OperandRole.RATE, precioDiaObj,
                        LocalDateTime.now(), LocalDateTime.now())
        );
        List<PayrollConceptOperand> plusTransporteOperands = List.of(
                new PayrollConceptOperand(null, plusTransporteObj, OperandRole.QUANTITY, diasPresenciaObj,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, plusTransporteObj, OperandRole.RATE, precioTransporteObj,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                if (!ruleSystemCode.equals(rs)) return Collections.emptyList();
                if ("SALARIO_BASE".equals(code))    return salarioBaseOperands;
                if ("PLUS_TRANSPORTE".equals(code)) return plusTransporteOperands;
                return Collections.emptyList();
            }
        };
    }

    // ── scenario 2: PLUS_TRANSPORTE ───────────────────────────────────────────

    @Test
    void plusTransporteSegmentOneIs105() {
        // T_PRECIO_TRANSPORTE = 7.50/day (fixed, no working-time scaling)
        // Segment 1: T_DIAS_PRESENCIA_SEGMENTO = 14 → PLUS_TRANSPORTE = 14 * 7.50 = 105.00
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("105.00").compareTo(
                result.getSegmentResults().get(0).getPlusTransporteAmount()),
                "segment 1 plusTransporte");
    }

    @Test
    void plusTransporteSegmentTwoIs120() {
        // Segment 2: T_DIAS_PRESENCIA_SEGMENTO = 16 → PLUS_TRANSPORTE = 16 * 7.50 = 120.00
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("120.00").compareTo(
                result.getSegmentResults().get(1).getPlusTransporteAmount()),
                "segment 2 plusTransporte");
    }

    @Test
    void totalPlusTransporteIs225() {
        // 105.00 + 120.00 = 225.00
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("225.00").compareTo(result.getTotalPlusTransporte()),
                "total plusTransporte");
    }

    @Test
    void plusTransporteIsIndependentOfWorkingTimePercentage() {
        // T_PRECIO_TRANSPORTE has no working-time scaling.
        // Segment 2 (16 days at 50%) exceeds segment 1 (14 days at 100%) for transport
        // because the day count, not the percentage, drives the transport allowance.
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        BigDecimal seg1 = result.getSegmentResults().get(0).getPlusTransporteAmount();
        BigDecimal seg2 = result.getSegmentResults().get(1).getPlusTransporteAmount();

        assertTrue(seg2.compareTo(seg1) > 0,
                "segment 2 plusTransporte (16d) must exceed segment 1 (14d) since rate is fixed");
    }

    @Test
    void salarioBaseAndPlusTransporteAreConsistentForSegmentOne() {
        // salarioBase = 933.33, plusTransporte = 105.00 → combined = 1038.33
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        SegmentExecutionResult seg1 = result.getSegmentResults().get(0);

        BigDecimal actualCombined = seg1.getSalarioBaseAmount().add(seg1.getPlusTransporteAmount());
        assertEquals(0, new BigDecimal("1038.33").compareTo(actualCombined),
                "segment 1: salarioBase + plusTransporte");
    }

    @Test
    void salarioBaseAndPlusTransporteAreConsistentForSegmentTwo() {
        // salarioBase = 533.33, plusTransporte = 120.00 → combined = 653.33
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        SegmentExecutionResult seg2 = result.getSegmentResults().get(1);

        BigDecimal actualCombined = seg2.getSalarioBaseAmount().add(seg2.getPlusTransporteAmount());
        assertEquals(0, new BigDecimal("653.33").compareTo(actualCombined),
                "segment 2: salarioBase + plusTransporte");
    }

    // ── scenario 3: TOTAL_DEVENGOS_SEGMENTO (AGGREGATE) ──────────────────────

    @Test
    void totalDevengosSegmentoSegmentOneIs1038_33() {
        // TOTAL_DEVENGOS_SEGMENTO (AGGREGATE) = SALARIO_BASE + PLUS_TRANSPORTE
        // Segment 1: 933.33 + 105.00 = 1038.33
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("1038.33").compareTo(
                result.getSegmentResults().get(0).getTotalDevengosSegmentoAmount()),
                "segment 1 TOTAL_DEVENGOS_SEGMENTO");
    }

    @Test
    void totalDevengosSegmentoSegmentTwoIs653_33() {
        // Segment 2: 533.33 + 120.00 = 653.33
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("653.33").compareTo(
                result.getSegmentResults().get(1).getTotalDevengosSegmentoAmount()),
                "segment 2 TOTAL_DEVENGOS_SEGMENTO");
    }

    @Test
    void totalDevengosConsolidatedIs1691_66() {
        // 1038.33 + 653.33 = 1691.66
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("1691.66").compareTo(result.getTotalDevengosConsolidated()),
                "totalDevengosConsolidated");
    }

    @Test
    void totalDevengosSegmentoMatchesSumOfItsSourcesForBothSegments() {
        // Verifies that AGGREGATE correctly sums SALARIO_BASE + PLUS_TRANSPORTE per segment.
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        for (SegmentExecutionResult seg : result.getSegmentResults()) {
            BigDecimal expected = seg.getSalarioBaseAmount().add(seg.getPlusTransporteAmount());
            assertEquals(0, expected.compareTo(seg.getTotalDevengosSegmentoAmount()),
                    "TOTAL_DEVENGOS_SEGMENTO must equal salarioBase + plusTransporte for each segment");
        }
    }
}
