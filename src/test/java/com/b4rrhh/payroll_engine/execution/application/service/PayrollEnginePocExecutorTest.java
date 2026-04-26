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
                    new DefaultConceptDependencyGraphService(pocFeedRelationRepo("ESP"), pocOperandRepo("ESP")),
                    new DefaultExecutionPlanBuilder(
                            pocOperandRepo("ESP"),
                            new OperandConfigurationValidator(),
                            pocFeedRelationRepo("ESP")),
                    new DefaultSegmentExecutionEngine(
                            new SegmentTechnicalValueResolver(),
                            new RateByQuantityOperandResolver(),
                            new PercentageConceptResolver(),
                            List.of()));

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
                        new DefaultConceptDependencyGraphService(emptyFeedRelationRepo(), emptyOperandRepo()),
                        new DefaultExecutionPlanBuilder(
                                emptyOperandRepo(),
                                new OperandConfigurationValidator(),
                                emptyFeedRelationRepo()),
                        new DefaultSegmentExecutionEngine(
                                new SegmentTechnicalValueResolver(),
                                new RateByQuantityOperandResolver(),
                                new PercentageConceptResolver(),
                            List.of()));
        assertThrows(MissingPocConceptException.class, () ->
                executorWithEmptyRepo.execute(referenceRequest()));
    }

    // ── repository stubs ─────────────────────────────────────────────────────

    /**
     * Returns a stub concept repository seeded with the 8 PoC concepts for the given rule system.
     * Concepts are assigned sequential IDs (1–8) so that feed relation lookup by ID works.
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
        PayrollConcept tPctIrpf = pocConcept(
                7L, ruleSystemCode, "T_PCT_IRPF",
                CalculationType.DIRECT_AMOUNT, FunctionalNature.INFORMATIONAL);
        PayrollConcept retencionIrpfTramo = pocConcept(
                8L, ruleSystemCode, "RETENCION_IRPF_TRAMO",
                CalculationType.PERCENTAGE, FunctionalNature.DEDUCTION);

        Map<String, PayrollConcept> index = new HashMap<>();
        index.put("T_DIAS_PRESENCIA_SEGMENTO", diasPresencia);
        index.put("T_PRECIO_DIA", precioDia);
        index.put("SALARIO_BASE", salarioBase);
        index.put("T_PRECIO_TRANSPORTE", precioTransporte);
        index.put("PLUS_TRANSPORTE", plusTransporte);
        index.put("TOTAL_DEVENGOS_SEGMENTO", totalDevengosSegmento);
        index.put("T_PCT_IRPF", tPctIrpf);
        index.put("RETENCION_IRPF_TRAMO", retencionIrpfTramo);

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
            @Override
            public java.util.List<PayrollConcept> findAllByCodes(String rs, java.util.Collection<String> codes) {
                if (!ruleSystemCode.equals(rs)) return java.util.List.of();
                return codes.stream().map(index::get).filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
            }
        };
    }

    /**
     * Returns a stub feed relation repository reflecting the full PoC structure:
     * <ul>
     *   <li>SALARIO_BASE (id=3) is fed by T_DIAS_PRESENCIA_SEGMENTO (id=1) and T_PRECIO_DIA (id=2).</li>
     *   <li>PLUS_TRANSPORTE (id=5) is fed by T_DIAS_PRESENCIA_SEGMENTO (id=1) and T_PRECIO_TRANSPORTE (id=4).</li>
     *   <li>TOTAL_DEVENGOS_SEGMENTO (id=6) is fed by SALARIO_BASE (id=3) and PLUS_TRANSPORTE (id=5).</li>
     *   <li>RETENCION_IRPF_TRAMO (id=8) is fed by TOTAL_DEVENGOS_SEGMENTO (id=6) and T_PCT_IRPF (id=7).</li>
     * </ul>
     */
    private static PayrollConceptFeedRelationRepository pocFeedRelationRepo(String ruleSystemCode) {
        PayrollObject diasPresenciaObj    = pocObject(1L, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject precioDiaObj        = pocObject(2L, ruleSystemCode, "T_PRECIO_DIA");
        PayrollObject salarioBaseObj      = pocObject(3L, ruleSystemCode, "SALARIO_BASE");
        PayrollObject precioTransporteObj = pocObject(4L, ruleSystemCode, "T_PRECIO_TRANSPORTE");
        PayrollObject plusTransporteObj   = pocObject(5L, ruleSystemCode, "PLUS_TRANSPORTE");
        PayrollObject totalDevengosObj    = pocObject(6L, ruleSystemCode, "TOTAL_DEVENGOS_SEGMENTO");
        PayrollObject tPctIrpfObj         = pocObject(7L, ruleSystemCode, "T_PCT_IRPF");
        PayrollObject retencionIrpfObj    = pocObject(8L, ruleSystemCode, "RETENCION_IRPF_TRAMO");

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
        List<PayrollConceptFeedRelation> retencionIrpfFeeds = List.of(
                feedRelation(totalDevengosObj, retencionIrpfObj),
                feedRelation(tPctIrpfObj, retencionIrpfObj)
        );

        Map<Long, List<PayrollConceptFeedRelation>> relsByTarget = new HashMap<>();
        relsByTarget.put(3L, salarioBaseFeeds);
        relsByTarget.put(5L, plusTransporteFeeds);
        relsByTarget.put(6L, totalDevengosFeeds);
        relsByTarget.put(8L, retencionIrpfFeeds);

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

    private static PayrollConceptOperandRepository emptyOperandRepo() {
        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) { return Collections.emptyList(); }
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
            @Override
            public java.util.List<PayrollConcept> findAllByCodes(String rs, java.util.Collection<String> codes) {
                return java.util.List.of();
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
                null, source, target, FeedMode.FEED_BY_SOURCE, null, false,
                LocalDate.of(2020, 1, 1), null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    /**
     * Returns a stub operand repository for all PoC RATE_BY_QUANTITY and PERCENTAGE concepts:
     * <ul>
     *   <li>SALARIO_BASE (id=3): QUANTITY=T_DIAS_PRESENCIA_SEGMENTO (id=1), RATE=T_PRECIO_DIA (id=2).</li>
     *   <li>PLUS_TRANSPORTE (id=5): QUANTITY=T_DIAS_PRESENCIA_SEGMENTO (id=1), RATE=T_PRECIO_TRANSPORTE (id=4).</li>
     *   <li>RETENCION_IRPF_TRAMO (id=8): BASE=TOTAL_DEVENGOS_SEGMENTO (id=6), PERCENTAGE=T_PCT_IRPF (id=7).</li>
     * </ul>
     */
    private static PayrollConceptOperandRepository pocOperandRepo(String ruleSystemCode) {
        PayrollObject diasPresenciaObj    = pocObject(1L, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject precioDiaObj        = pocObject(2L, ruleSystemCode, "T_PRECIO_DIA");
        PayrollObject salarioBaseObj      = pocObject(3L, ruleSystemCode, "SALARIO_BASE");
        PayrollObject precioTransporteObj = pocObject(4L, ruleSystemCode, "T_PRECIO_TRANSPORTE");
        PayrollObject plusTransporteObj   = pocObject(5L, ruleSystemCode, "PLUS_TRANSPORTE");
        PayrollObject totalDevengosObj    = pocObject(6L, ruleSystemCode, "TOTAL_DEVENGOS_SEGMENTO");
        PayrollObject tPctIrpfObj         = pocObject(7L, ruleSystemCode, "T_PCT_IRPF");
        PayrollObject retencionIrpfObj    = pocObject(8L, ruleSystemCode, "RETENCION_IRPF_TRAMO");

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
        List<PayrollConceptOperand> retencionIrpfOperands = List.of(
                new PayrollConceptOperand(null, retencionIrpfObj, OperandRole.BASE, totalDevengosObj,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, retencionIrpfObj, OperandRole.PERCENTAGE, tPctIrpfObj,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                if (!ruleSystemCode.equals(rs)) return Collections.emptyList();
                return switch (code) {
                    case "SALARIO_BASE"         -> salarioBaseOperands;
                    case "PLUS_TRANSPORTE"      -> plusTransporteOperands;
                    case "RETENCION_IRPF_TRAMO" -> retencionIrpfOperands;
                    default                     -> Collections.emptyList();
                };
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

    // ── scenario 4: RETENCION_IRPF_TRAMO (PERCENTAGE) ────────────────────────

    @Test
    void retencionIrpfSegmentOneIs155_75() {
        // T_PCT_IRPF = 15 (fixed)
        // Segment 1: TOTAL_DEVENGOS_SEGMENTO = 1038.33 → RETENCION = 1038.33 × 15 / 100 = 155.7495 → 155.75
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("155.75").compareTo(
                result.getSegmentResults().get(0).getRetencionIrpfTramoAmount()),
                "segment 1 RETENCION_IRPF_TRAMO");
    }

    @Test
    void retencionIrpfSegmentTwoIs98_00() {
        // Segment 2: TOTAL_DEVENGOS_SEGMENTO = 653.33 → RETENCION = 653.33 × 15 / 100 = 97.9995 → 98.00
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("98.00").compareTo(
                result.getSegmentResults().get(1).getRetencionIrpfTramoAmount()),
                "segment 2 RETENCION_IRPF_TRAMO");
    }

    @Test
    void totalRetencionIrpfIs253_75() {
        // 155.75 + 98.00 = 253.75
        PayrollEnginePocResult result = executor.execute(referenceRequest());

        assertEquals(0, new BigDecimal("253.75").compareTo(result.getTotalRetencionIrpf()),
                "totalRetencionIrpf");
    }

    @Test
    void retencionIrpfIsConsistentWithTotalDevengosForBothSegments() {
        // Verifies RETENCION = TOTAL_DEVENGOS × 15 / 100 per segment (independent of arithmetic path).
        PayrollEnginePocResult result = executor.execute(referenceRequest());
        for (SegmentExecutionResult seg : result.getSegmentResults()) {
            BigDecimal expected = seg.getTotalDevengosSegmentoAmount()
                    .multiply(new BigDecimal("15"))
                    .divide(new java.math.BigDecimal("100"), 8, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            assertEquals(0, expected.compareTo(seg.getRetencionIrpfTramoAmount()),
                    "RETENCION_IRPF_TRAMO must equal TOTAL_DEVENGOS × 15 / 100 for each segment");
        }
    }
}
