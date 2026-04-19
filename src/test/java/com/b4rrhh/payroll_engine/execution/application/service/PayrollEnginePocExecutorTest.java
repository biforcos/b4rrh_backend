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
                    new DefaultExecutionPlanBuilder(),
                    new DefaultSegmentExecutionEngine(
                            new SegmentTechnicalValueResolver(),
                            new RateByQuantityOperandResolver(
                                    pocOperandRepo("ESP"),
                                    new RateByQuantityConfigurationValidator())));

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

    @Test
    void missingPocConceptThrowsMissingPocConceptException() {
        DefaultPayrollEnginePocExecutor executorWithEmptyRepo =
                new DefaultPayrollEnginePocExecutor(
                        new DefaultWorkingTimeSegmentBuilder(),
                        emptyConceptRepository(),
                        new DefaultConceptDependencyGraphService(emptyFeedRelationRepo()),
                        new DefaultExecutionPlanBuilder(),
                        new DefaultSegmentExecutionEngine(
                                new SegmentTechnicalValueResolver(),
                                new RateByQuantityOperandResolver(
                                        new PayrollConceptOperandRepository() {
                                            @Override
                                            public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
                                            @Override
                                            public List<PayrollConceptOperand> findByTarget(String rs, String code) { return Collections.emptyList(); }
                                        },
                                        new RateByQuantityConfigurationValidator())));
        assertThrows(MissingPocConceptException.class, () ->
                executorWithEmptyRepo.execute(referenceRequest()));
    }

    // ── repository stubs ─────────────────────────────────────────────────────

    /**
     * Returns a stub concept repository seeded with the 3 PoC concepts for the given rule system.
     * Concepts are assigned sequential IDs (1, 2, 3) so that feed relation lookup by ID works.
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

        Map<String, PayrollConcept> index = new HashMap<>();
        index.put("T_DIAS_PRESENCIA_SEGMENTO", diasPresencia);
        index.put("T_PRECIO_DIA", precioDia);
        index.put("SALARIO_BASE", salarioBase);

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
     * Returns a stub feed relation repository reflecting the PoC structure:
     * SALARIO_BASE (id=3) is fed by T_DIAS_PRESENCIA_SEGMENTO (id=1) and T_PRECIO_DIA (id=2).
     */
    private static PayrollConceptFeedRelationRepository pocFeedRelationRepo(String ruleSystemCode) {
        PayrollObject diasPresenciaObj = pocObject(1L, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject precioDiaObj    = pocObject(2L, ruleSystemCode, "T_PRECIO_DIA");
        PayrollObject salarioBaseObj  = pocObject(3L, ruleSystemCode, "SALARIO_BASE");

        List<PayrollConceptFeedRelation> salarioBaseFeeds = List.of(
                feedRelation(diasPresenciaObj, salarioBaseObj),
                feedRelation(precioDiaObj, salarioBaseObj)
        );

        Map<Long, List<PayrollConceptFeedRelation>> relsByTarget = new HashMap<>();
        relsByTarget.put(3L, salarioBaseFeeds);

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
     * Returns a stub operand repository for the PoC concepts:
     * SALARIO_BASE (id=3) has QUANTITY=T_DIAS_PRESENCIA_SEGMENTO (id=1) and RATE=T_PRECIO_DIA (id=2).
     */
    private static PayrollConceptOperandRepository pocOperandRepo(String ruleSystemCode) {
        PayrollObject targetObj = pocObject(3L, ruleSystemCode, "SALARIO_BASE");
        PayrollObject qObj      = pocObject(1L, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject rObj      = pocObject(2L, ruleSystemCode, "T_PRECIO_DIA");

        List<PayrollConceptOperand> salarioBaseOperands = List.of(
                new PayrollConceptOperand(null, targetObj, OperandRole.QUANTITY, qObj,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, targetObj, OperandRole.RATE, rObj,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                return ruleSystemCode.equals(rs) && "SALARIO_BASE".equals(code)
                        ? salarioBaseOperands
                        : Collections.emptyList();
            }
        };
    }
}
