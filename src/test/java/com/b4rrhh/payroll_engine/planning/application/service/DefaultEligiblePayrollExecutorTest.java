package com.b4rrhh.payroll_engine.planning.application.service;

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
import com.b4rrhh.payroll_engine.eligibility.application.service.DefaultConceptEligibilityResolver;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ConceptAssignment;
import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.eligibility.domain.port.ConceptAssignmentRepository;
import com.b4rrhh.payroll_engine.execution.application.service.DefaultExecutionPlanBuilder;
import com.b4rrhh.payroll_engine.execution.application.service.DefaultSegmentExecutionEngine;
import com.b4rrhh.payroll_engine.execution.application.service.OperandConfigurationValidator;
import com.b4rrhh.payroll_engine.execution.application.service.PercentageConceptResolver;
import com.b4rrhh.payroll_engine.execution.application.service.RateByQuantityOperandResolver;
import com.b4rrhh.payroll_engine.execution.application.service.SegmentTechnicalValueResolver;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.planning.domain.exception.MissingEligibleConceptDefinitionException;
import com.b4rrhh.payroll_engine.segment.application.service.DefaultWorkingTimeSegmentBuilder;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultEligiblePayrollExecutor}.
 *
 * <p>All collaborators are replaced with in-memory fakes — no Spring context or JPA is needed.
 * The test uses the full real execution stack (dependency graph, plan builder, segment engine)
 * but drives concept eligibility through a controllable fake assignment repository.
 *
 * <h3>PoC scenario under test</h3>
 * <p>April 2026, 30 days, €2000/month. Employee works 14 days at 100% then 16 days at 50%.
 * Expected business concept totals:
 * <ul>
 *   <li>totalSalarioBase = 1466.66</li>
 *   <li>totalPlusTransporte = 225.00</li>
 *   <li>totalDevengosConsolidated = 1691.66</li>
 *   <li>totalRetencionIrpf = 253.75</li>
 * </ul>
 *
 * <p>These are identical to the {@code PayrollEnginePocExecutorTest} reference values — the only
 * difference is that concepts are selected via eligibility rather than hardcoded loading.
 */
class DefaultEligiblePayrollExecutorTest {

    private static final String RS = "ESP";
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 0, 0);

    // April 2026 split dates
    private static final LocalDate APR_01 = LocalDate.of(2026, 4, 1);
    private static final LocalDate APR_14 = LocalDate.of(2026, 4, 14);
    private static final LocalDate APR_15 = LocalDate.of(2026, 4, 15);
    private static final LocalDate APR_30 = LocalDate.of(2026, 4, 30);

    // concept IDs (used to wire feed relations)
    private static final long ID_T_DIAS              = 1L;
    private static final long ID_T_PRECIO_DIA        = 2L;
    private static final long ID_SALARIO_BASE        = 3L;
    private static final long ID_T_PRECIO_TRANSPORTE = 4L;
    private static final long ID_PLUS_TRANSPORTE     = 5L;
    private static final long ID_TOTAL_DEVENGOS      = 6L;
    private static final long ID_T_PCT_IRPF          = 7L;
    private static final long ID_RETENCION_IRPF      = 8L;

    /**
     * Builds a fully wired {@link DefaultEligiblePayrollExecutor} whose eligibility layer
     * returns the given set of concept codes.
     *
     * <p>The underlying concept repository contains all 8 PoC concepts with correct
     * {@link CalculationType} values, so expansion and plan building work correctly.
     */
    private DefaultEligiblePayrollExecutor executorWithEligibleCodes(String... eligibleCodes) {
        PayrollConcept tDias             = concept(ID_T_DIAS,              "T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT,    FunctionalNature.INFORMATIONAL);
        PayrollConcept tPrecioDia        = concept(ID_T_PRECIO_DIA,        "T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT,    FunctionalNature.INFORMATIONAL);
        PayrollConcept salarioBase       = concept(ID_SALARIO_BASE,        "SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY, FunctionalNature.EARNING);
        PayrollConcept tPrecioTransporte = concept(ID_T_PRECIO_TRANSPORTE, "T_PRECIO_TRANSPORTE",       CalculationType.DIRECT_AMOUNT,    FunctionalNature.INFORMATIONAL);
        PayrollConcept plusTransporte    = concept(ID_PLUS_TRANSPORTE,     "PLUS_TRANSPORTE",           CalculationType.RATE_BY_QUANTITY, FunctionalNature.EARNING);
        PayrollConcept totalDevengos     = concept(ID_TOTAL_DEVENGOS,      "TOTAL_DEVENGOS_SEGMENTO",   CalculationType.AGGREGATE,        FunctionalNature.EARNING);
        PayrollConcept tPctIrpf          = concept(ID_T_PCT_IRPF,          "T_PCT_IRPF",                CalculationType.DIRECT_AMOUNT,    FunctionalNature.INFORMATIONAL);
        PayrollConcept retencionIrpf     = concept(ID_RETENCION_IRPF,      "RETENCION_IRPF_TRAMO",      CalculationType.PERCENTAGE,       FunctionalNature.DEDUCTION);

        PayrollConceptRepository conceptRepo = new FakeConceptRepository(List.of(
                tDias, tPrecioDia, salarioBase, tPrecioTransporte,
                plusTransporte, totalDevengos, tPctIrpf, retencionIrpf
        ));

        // Feed relations: target ← sources
        //   SALARIO_BASE(3)            ← T_DIAS(1), T_PRECIO_DIA(2)
        //   PLUS_TRANSPORTE(5)         ← T_DIAS(1), T_PRECIO_TRANSPORTE(4)
        //   TOTAL_DEVENGOS(6)          ← SALARIO_BASE(3), PLUS_TRANSPORTE(5)
        //   RETENCION_IRPF_TRAMO(8)   ← TOTAL_DEVENGOS(6), T_PCT_IRPF(7)
        PayrollConceptFeedRelationRepository feedRepo = new FakeFeedRelationRepository(Map.of(
                ID_SALARIO_BASE,    List.of(feedRel(tDias, salarioBase), feedRel(tPrecioDia, salarioBase)),
                ID_PLUS_TRANSPORTE, List.of(feedRel(tDias, plusTransporte), feedRel(tPrecioTransporte, plusTransporte)),
                ID_TOTAL_DEVENGOS,  List.of(feedRel(salarioBase, totalDevengos), feedRel(plusTransporte, totalDevengos)),
                ID_RETENCION_IRPF,  List.of(feedRel(totalDevengos, retencionIrpf), feedRel(tPctIrpf, retencionIrpf))
        ));

        PayrollConceptOperandRepository operandRepo = pocOperandRepo();

        ConceptAssignmentRepository assignmentRepo = fixedAssignmentRepo(RS, eligibleCodes);
        DefaultConceptEligibilityResolver eligibilityResolver = new DefaultConceptEligibilityResolver(assignmentRepo);
        DefaultEligibleConceptExpansionService expansionService =
                new DefaultEligibleConceptExpansionService(conceptRepo, feedRepo, operandRepo);
        DefaultConceptDependencyGraphService graphService =
                new DefaultConceptDependencyGraphService(feedRepo, operandRepo);
        DefaultExecutionPlanBuilder planBuilder =
                new DefaultExecutionPlanBuilder(operandRepo, new OperandConfigurationValidator(), feedRepo);

        BuildEligibleExecutionPlanUseCase planUseCase = new DefaultEligibleExecutionPlanBuilder(
                eligibilityResolver, conceptRepo, expansionService, graphService, planBuilder);

        return new DefaultEligiblePayrollExecutor(
                planUseCase,
                new DefaultWorkingTimeSegmentBuilder(),
                new DefaultSegmentExecutionEngine(
                        new SegmentTechnicalValueResolver(),
                        new RateByQuantityOperandResolver(),
                        new PercentageConceptResolver(),
                        List.of()));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private EligiblePayrollExecutionRequest referenceRequest() {
        return new EligiblePayrollExecutionRequest(
                RS, "EMP", "EMP0001", "EMP1", "METAL",
                APR_01, APR_30,
                new BigDecimal("2000.00"),
                List.of(
                        new WorkingTimeWindow(APR_01, APR_14, new BigDecimal("100")),
                        new WorkingTimeWindow(APR_15, null, new BigDecimal("50"))
                )
        );
    }

    // ── Test 1: end-to-end with 3 eligible business concepts ─────────────────

    @Test
    void fullExecution_eligibilityDrivesConceptSelection_totalsMatchPocValues() {
        // Eligibility provides only the 3 business concepts.
        // Technical concepts (T_DIAS_PRESENCIA_SEGMENTO, T_PRECIO_DIA, etc.) must come via expansion.
        DefaultEligiblePayrollExecutor executor =
                executorWithEligibleCodes("SALARIO_BASE", "PLUS_TRANSPORTE", "RETENCION_IRPF_TRAMO");

        EligiblePayrollExecutionResult result = executor.execute(referenceRequest());

        // Verify two segments were produced
        assertEquals(2, result.getSegmentResults().size());

        // April 2026: 30 days, 2000€/month
        // Segment 1: 14 days at 100% → salarioBase = 933.33
        // Segment 2: 16 days at 50%  → salarioBase = 533.33
        assertEquals(0, new BigDecimal("1466.66").compareTo(result.getTotalSalarioBase()),
                "totalSalarioBase");
        assertEquals(0, new BigDecimal("225.00").compareTo(result.getTotalPlusTransporte()),
                "totalPlusTransporte");
        assertEquals(0, new BigDecimal("1691.66").compareTo(result.getTotalDevengosConsolidated()),
                "totalDevengosConsolidated");
        assertEquals(0, new BigDecimal("253.75").compareTo(result.getTotalRetencionIrpf()),
                "totalRetencionIrpf");
    }

    // ── Test 2: auditable result carries full planning context ─────────────

    @Test
    void result_carriesAuditableContext_includesAssignmentsAndExpansion() {
        DefaultEligiblePayrollExecutor executor =
                executorWithEligibleCodes("SALARIO_BASE", "PLUS_TRANSPORTE", "RETENCION_IRPF_TRAMO");

        EligiblePayrollExecutionResult result = executor.execute(referenceRequest());

        // Context is set
        assertNotNull(result.getContext());
        assertEquals(RS, result.getContext().getRuleSystemCode());
        // Current contract: referenceDate is derived from periodStart.
        assertEquals(APR_01, result.getReferenceDate());

        // Eligible concepts: only the 3 business concepts (not the technical ones)
        assertEquals(3, result.getPlanningResult().eligibleConcepts().size());
        var eligibleCodes = result.getPlanningResult().eligibleConcepts().stream()
                .map(PayrollConcept::getConceptCode)
                .collect(Collectors.toSet());
        assertTrue(eligibleCodes.contains("SALARIO_BASE"));
        assertTrue(eligibleCodes.contains("PLUS_TRANSPORTE"));
        assertTrue(eligibleCodes.contains("RETENCION_IRPF_TRAMO"));

        // Expanded concepts: all 8 (eligible + technical dependencies)
        assertEquals(8, result.getPlanningResult().expandedConcepts().size(),
                "technical concepts must be pulled in by expansion, not by eligibility");
    }

    // ── Test 3: empty eligibility → empty plan → zero totals ─────────────────

    @Test
    void emptyEligibility_producesZeroTotalsAndNoSegmentResults() {
        // No concept assignments → empty plan
        DefaultEligiblePayrollExecutor executor = executorWithEligibleCodes(/* none */);

        EligiblePayrollExecutionResult result = executor.execute(referenceRequest());

        assertTrue(result.getSegmentResults().isEmpty(), "no segment results expected for empty plan");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalSalarioBase()),
                "totalSalarioBase");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalPlusTransporte()),
                "totalPlusTransporte");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalDevengosConsolidated()),
                "totalDevengosConsolidated");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalRetencionIrpf()),
                "totalRetencionIrpf");
        assertTrue(result.getPlanningResult().executionPlan().isEmpty(),
                "execution plan must be empty");
    }

    // ── Test 4: missing concept definition → exception propagates ─────────────

    @Test
    void missingConceptDefinition_throwsMissingEligibleConceptDefinitionException() {
        // Eligibility resolves GHOST_CONCEPT but no definition exists in the repo
        DefaultEligiblePayrollExecutor executor =
                executorWithEligibleCodes("SALARIO_BASE", "GHOST_CONCEPT");

        assertThrows(MissingEligibleConceptDefinitionException.class,
                () -> executor.execute(referenceRequest()));
    }

    // ── concept factory ───────────────────────────────────────────────────────

    private static PayrollConcept concept(long id, String code,
                                          CalculationType calcType,
                                          FunctionalNature nature) {
        PayrollObject obj = new PayrollObject(id, RS, PayrollObjectTypeCode.CONCEPT, code, NOW, NOW);
        return new PayrollConcept(obj, code, calcType, nature, ResultCompositionMode.REPLACE,
                null, ExecutionScope.SEGMENT, NOW, NOW);
    }

    private static PayrollConceptFeedRelation feedRel(PayrollConcept source, PayrollConcept target) {
        return new PayrollConceptFeedRelation(
                null,
                source.getObject(),
                target.getObject(),
                FeedMode.FEED_BY_SOURCE,
                null, false,
                LocalDate.of(2020, 1, 1),
                null,
                NOW,
                NOW
        );
    }

    private static PayrollObject pocObject(Long id, String code) {
        return new PayrollObject(id, RS, PayrollObjectTypeCode.CONCEPT, code, NOW, NOW);
    }

    /**
     * Returns a stub operand repository for all PoC RATE_BY_QUANTITY and PERCENTAGE concepts:
     * <ul>
     *   <li>SALARIO_BASE (id=3): QUANTITY=T_DIAS_PRESENCIA_SEGMENTO (id=1), RATE=T_PRECIO_DIA (id=2).</li>
     *   <li>PLUS_TRANSPORTE (id=5): QUANTITY=T_DIAS_PRESENCIA_SEGMENTO (id=1), RATE=T_PRECIO_TRANSPORTE (id=4).</li>
     *   <li>RETENCION_IRPF_TRAMO (id=8): BASE=TOTAL_DEVENGOS_SEGMENTO (id=6), PERCENTAGE=T_PCT_IRPF (id=7).</li>
     * </ul>
     */
    private static PayrollConceptOperandRepository pocOperandRepo() {
        PayrollObject diasPresenciaObj    = pocObject(ID_T_DIAS,              "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject precioDiaObj        = pocObject(ID_T_PRECIO_DIA,        "T_PRECIO_DIA");
        PayrollObject salarioBaseObj      = pocObject(ID_SALARIO_BASE,        "SALARIO_BASE");
        PayrollObject precioTransporteObj = pocObject(ID_T_PRECIO_TRANSPORTE, "T_PRECIO_TRANSPORTE");
        PayrollObject plusTransporteObj   = pocObject(ID_PLUS_TRANSPORTE,     "PLUS_TRANSPORTE");
        PayrollObject totalDevengosObj    = pocObject(ID_TOTAL_DEVENGOS,      "TOTAL_DEVENGOS_SEGMENTO");
        PayrollObject tPctIrpfObj         = pocObject(ID_T_PCT_IRPF,          "T_PCT_IRPF");
        PayrollObject retencionIrpfObj    = pocObject(ID_RETENCION_IRPF,      "RETENCION_IRPF_TRAMO");

        List<PayrollConceptOperand> salarioBaseOperands = List.of(
                new PayrollConceptOperand(null, salarioBaseObj, OperandRole.QUANTITY, diasPresenciaObj, NOW, NOW),
                new PayrollConceptOperand(null, salarioBaseObj, OperandRole.RATE,     precioDiaObj,     NOW, NOW)
        );
        List<PayrollConceptOperand> plusTransporteOperands = List.of(
                new PayrollConceptOperand(null, plusTransporteObj, OperandRole.QUANTITY, diasPresenciaObj,    NOW, NOW),
                new PayrollConceptOperand(null, plusTransporteObj, OperandRole.RATE,     precioTransporteObj, NOW, NOW)
        );
        List<PayrollConceptOperand> retencionIrpfOperands = List.of(
                new PayrollConceptOperand(null, retencionIrpfObj, OperandRole.BASE,       totalDevengosObj, NOW, NOW),
                new PayrollConceptOperand(null, retencionIrpfObj, OperandRole.PERCENTAGE, tPctIrpfObj,      NOW, NOW)
        );

        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }

            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                if (!RS.equals(rs)) return Collections.emptyList();
                return switch (code) {
                    case "SALARIO_BASE"         -> salarioBaseOperands;
                    case "PLUS_TRANSPORTE"      -> plusTransporteOperands;
                    case "RETENCION_IRPF_TRAMO" -> retencionIrpfOperands;
                    default                     -> Collections.emptyList();
                };
            }
        };
    }

    private static ConceptAssignmentRepository fixedAssignmentRepo(String ruleSystemCode,
                                                                    String... conceptCodes) {
        List<ConceptAssignment> assignments = new java.util.ArrayList<>();
        for (String code : conceptCodes) {
            assignments.add(new ConceptAssignment(
                    null, ruleSystemCode, code, null, null, null,
                    LocalDate.of(2025, 1, 1), null, 0, NOW, NOW
            ));
        }
        return new ConceptAssignmentRepository() {
            @Override
            public ConceptAssignment save(ConceptAssignment a) { throw new UnsupportedOperationException(); }

            @Override
            public List<ConceptAssignment> findApplicableAssignments(EmployeeAssignmentContext ctx, LocalDate date) {
                return assignments;
            }
        };
    }

    // ── in-memory fakes ───────────────────────────────────────────────────────

    private static final class FakeConceptRepository implements PayrollConceptRepository {

        private final Map<String, PayrollConcept> byCode;

        FakeConceptRepository(List<PayrollConcept> concepts) {
            this.byCode = concepts.stream()
                    .collect(Collectors.toMap(PayrollConcept::getConceptCode, c -> c));
        }

        @Override
        public Optional<PayrollConcept> findByBusinessKey(String rs, String code) {
            return Optional.ofNullable(byCode.get(code));
        }

        @Override
        public List<PayrollConcept> findAllByCodes(String rs, Collection<String> codes) {
            return codes.stream()
                    .map(byCode::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        }

        @Override
        public PayrollConcept save(PayrollConcept c) { throw new UnsupportedOperationException(); }

        @Override
        public boolean existsByBusinessKey(String rs, String code) {
            return byCode.containsKey(code);
        }
    }

    private static final class FakeFeedRelationRepository implements PayrollConceptFeedRelationRepository {

        private final Map<Long, List<PayrollConceptFeedRelation>> byTargetId;

        FakeFeedRelationRepository(Map<Long, List<PayrollConceptFeedRelation>> byTargetId) {
            this.byTargetId = new HashMap<>(byTargetId);
        }

        @Override
        public PayrollConceptFeedRelation save(PayrollConceptFeedRelation r) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PayrollConceptFeedRelation> findActiveByTargetObjectId(Long id, LocalDate date) {
            return byTargetId.getOrDefault(id, List.of());
        }
    }
}
