package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.ExecutionScope;
import com.b4rrhh.payroll_engine.concept.domain.model.FunctionalNature;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.model.ResultCompositionMode;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraphBuilder;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptResultException;
import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedCalculationTypeException;
import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedTechnicalConceptException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultSegmentExecutionEngine}.
 *
 * <p>Reference values:
 * <ul>
 *   <li>Monthly salary: 2000 €, 30 days in period, 14 days in segment, 100% working time</li>
 *   <li>T_DIAS_PRESENCIA_SEGMENTO = 14</li>
 *   <li>T_PRECIO_DIA = 2000 / 30 * 100/100 = 66.66666667 (scale 8 HALF_UP)</li>
 *   <li>SALARIO_BASE = 14 * 66.66666667 = 933.33 (scale 2 HALF_UP)</li>
 * </ul>
 */
class SegmentExecutionEngineTest {

    private static final String RULE_SYSTEM = "ESP";

    /**
     * In-memory operand stub: SALARIO_BASE has QUANTITY=T_DIAS_PRESENCIA_SEGMENTO, RATE=T_PRECIO_DIA.
     */
    private static PayrollConceptOperandRepository pocOperandRepo() {
        PayrollObject targetObj = new PayrollObject(3L, RULE_SYSTEM, PayrollObjectTypeCode.CONCEPT,
                "SALARIO_BASE", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject qObj = new PayrollObject(1L, RULE_SYSTEM, PayrollObjectTypeCode.CONCEPT,
                "T_DIAS_PRESENCIA_SEGMENTO", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject rObj = new PayrollObject(2L, RULE_SYSTEM, PayrollObjectTypeCode.CONCEPT,
                "T_PRECIO_DIA", LocalDateTime.now(), LocalDateTime.now());

        List<PayrollConceptOperand> salarioBaseOperands = List.of(
                new PayrollConceptOperand(null, targetObj, OperandRole.QUANTITY, qObj,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, targetObj, OperandRole.RATE, rObj,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                return "SALARIO_BASE".equals(code) ? salarioBaseOperands : List.of();
            }
        };
    }

    private final DefaultSegmentExecutionEngine engine =
            new DefaultSegmentExecutionEngine(
                    new SegmentTechnicalValueResolver(),
                    new RateByQuantityOperandResolver(
                            pocOperandRepo(),
                            new RateByQuantityConfigurationValidator()));

    /**
     * Builds a minimal PoC graph: SALARIO_BASE depends on T_DIAS_PRESENCIA_SEGMENTO and T_PRECIO_DIA.
     */
    private static ConceptDependencyGraph pocGraph() {
        PayrollConcept dias = pocConcept(1L, "T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio = pocConcept(2L, "T_PRECIO_DIA", CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = pocConcept(3L, "SALARIO_BASE", CalculationType.RATE_BY_QUANTITY);
        return new ConceptDependencyGraphBuilder()
                .addOperandDependency(salario, dias)
                .addOperandDependency(salario, precio)
                .build();
    }

    private static PayrollConcept pocConcept(Long id, String code, CalculationType type) {
        PayrollObject obj = new PayrollObject(id, RULE_SYSTEM, PayrollObjectTypeCode.CONCEPT, code, null, null);
        return new PayrollConcept(obj, code, type, FunctionalNature.INFORMATIONAL,
                ResultCompositionMode.REPLACE, null, ExecutionScope.SEGMENT, null, null);
    }

    private static ConceptNodeIdentity node(String code) {
        return new ConceptNodeIdentity(RULE_SYSTEM, code);
    }

    private static SegmentCalculationContext context100pct(int daysInPeriod, int daysInSegment, BigDecimal salary) {
        LocalDate periodStart = LocalDate.of(2026, 4, 1);
        LocalDate periodEnd   = LocalDate.of(2026, 4, 30);
        LocalDate segStart    = LocalDate.of(2026, 4, 1);
        LocalDate segEnd      = segStart.plusDays(daysInSegment - 1);
        return new SegmentCalculationContext(
                RULE_SYSTEM, "EMP", "EMP0001",
                periodStart, periodEnd, segStart, segEnd,
                true, true,
                daysInPeriod, daysInSegment,
                new BigDecimal("100"),
                salary
        );
    }

    // ── T_DIAS_PRESENCIA_SEGMENTO (DIRECT_AMOUNT) ────────────────────────────

    @Test
    void directAmountDiasPresenciaReturnsSegmentDays() {
        SegmentCalculationContext ctx = context100pct(30, 14, new BigDecimal("2000.00"));
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("T_DIAS_PRESENCIA_SEGMENTO"), CalculationType.DIRECT_AMOUNT)
        );

        SegmentExecutionState state = engine.execute(plan, ctx, pocGraph());

        assertEquals(0, new BigDecimal("14").compareTo(
                state.getRequiredAmount(node("T_DIAS_PRESENCIA_SEGMENTO"))));
    }

    // ── T_PRECIO_DIA (DIRECT_AMOUNT) ─────────────────────────────────────────

    @Test
    void directAmountPrecioDiaAt100PctIsCorrect() {
        // 2000 / 30 * 1.0 = 66.66666667 (scale 8)
        SegmentCalculationContext ctx = context100pct(30, 14, new BigDecimal("2000.00"));
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("T_PRECIO_DIA"), CalculationType.DIRECT_AMOUNT)
        );

        SegmentExecutionState state = engine.execute(plan, ctx, pocGraph());

        assertEquals(0, new BigDecimal("66.66666667").compareTo(
                state.getRequiredAmount(node("T_PRECIO_DIA"))));
    }

    @Test
    void directAmountPrecioDiaAt50PctIsHalfOfFull() {
        // 2000 / 30 * 0.5 = 33.33333333
        LocalDate s = LocalDate.of(2026, 4, 15);
        LocalDate e = LocalDate.of(2026, 4, 30);
        SegmentCalculationContext ctx = new SegmentCalculationContext(
                RULE_SYSTEM, "EMP", "EMP0001",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                s, e, false, true, 30, 16,
                new BigDecimal("50"),
                new BigDecimal("2000.00")
        );
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("T_PRECIO_DIA"), CalculationType.DIRECT_AMOUNT)
        );

        SegmentExecutionState state = engine.execute(plan, ctx, pocGraph());

        assertEquals(0, new BigDecimal("33.33333334").compareTo(
                state.getRequiredAmount(node("T_PRECIO_DIA"))));
    }

    // ── SALARIO_BASE (RATE_BY_QUANTITY) ──────────────────────────────────────

    @Test
    void salarioBaseIsComputedAfterDependencies() {
        // 14 * 66.66666667 = 933.33333338 → scale 2 HALF_UP = 933.33
        SegmentCalculationContext ctx = context100pct(30, 14, new BigDecimal("2000.00"));
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("T_DIAS_PRESENCIA_SEGMENTO"), CalculationType.DIRECT_AMOUNT),
                new ConceptExecutionPlanEntry(node("T_PRECIO_DIA"), CalculationType.DIRECT_AMOUNT),
                new ConceptExecutionPlanEntry(node("SALARIO_BASE"), CalculationType.RATE_BY_QUANTITY)
        );

        SegmentExecutionState state = engine.execute(plan, ctx, pocGraph());

        assertEquals(0, new BigDecimal("933.33").compareTo(
                state.getRequiredAmount(node("SALARIO_BASE"))));
    }

    @Test
    void salarioBaseAt50PctIs533_33() {
        // 16 * 33.33333333 = 533.33333328 → scale 2 HALF_UP = 533.33
        LocalDate s = LocalDate.of(2026, 4, 15);
        LocalDate e = LocalDate.of(2026, 4, 30);
        SegmentCalculationContext ctx = new SegmentCalculationContext(
                RULE_SYSTEM, "EMP", "EMP0001",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                s, e, false, true, 30, 16,
                new BigDecimal("50"),
                new BigDecimal("2000.00")
        );
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("T_DIAS_PRESENCIA_SEGMENTO"), CalculationType.DIRECT_AMOUNT),
                new ConceptExecutionPlanEntry(node("T_PRECIO_DIA"), CalculationType.DIRECT_AMOUNT),
                new ConceptExecutionPlanEntry(node("SALARIO_BASE"), CalculationType.RATE_BY_QUANTITY)
        );

        SegmentExecutionState state = engine.execute(plan, ctx, pocGraph());

        assertEquals(0, new BigDecimal("533.33").compareTo(
                state.getRequiredAmount(node("SALARIO_BASE"))));
    }

    // ── error cases ──────────────────────────────────────────────────────────

    @Test
    void salarioBaseMissingDependencyThrows() {
        SegmentCalculationContext ctx = context100pct(30, 14, new BigDecimal("2000.00"));
        // SALARIO_BASE first — its dependencies are not in state yet
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("SALARIO_BASE"), CalculationType.RATE_BY_QUANTITY)
        );

        assertThrows(MissingConceptResultException.class, () -> engine.execute(plan, ctx, pocGraph()));
    }

    @Test
    void unsupportedCalculationTypeThrows() {
        SegmentCalculationContext ctx = context100pct(30, 14, new BigDecimal("2000.00"));
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("SOME_CONCEPT"), CalculationType.AGGREGATE)
        );

        assertThrows(UnsupportedCalculationTypeException.class, () -> engine.execute(plan, ctx, pocGraph()));
    }

    @Test
    void unsupportedDirectAmountConceptCodeThrows() {
        SegmentCalculationContext ctx = context100pct(30, 14, new BigDecimal("2000.00"));
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("UNKNOWN_CONCEPT"), CalculationType.DIRECT_AMOUNT)
        );

        assertThrows(UnsupportedTechnicalConceptException.class, () -> engine.execute(plan, ctx, pocGraph()));
    }

    @Test
    void stateContainsAllThreeConceptsFromFullPlan() {
        SegmentCalculationContext ctx = context100pct(30, 14, new BigDecimal("2000.00"));
        List<ConceptExecutionPlanEntry> plan = List.of(
                new ConceptExecutionPlanEntry(node("T_DIAS_PRESENCIA_SEGMENTO"), CalculationType.DIRECT_AMOUNT),
                new ConceptExecutionPlanEntry(node("T_PRECIO_DIA"), CalculationType.DIRECT_AMOUNT),
                new ConceptExecutionPlanEntry(node("SALARIO_BASE"), CalculationType.RATE_BY_QUANTITY)
        );

        SegmentExecutionState state = engine.execute(plan, ctx, pocGraph());

        assertTrue(state.getOptionalAmount(node("T_DIAS_PRESENCIA_SEGMENTO")).isPresent());
        assertTrue(state.getOptionalAmount(node("T_PRECIO_DIA")).isPresent());
        assertTrue(state.getOptionalAmount(node("SALARIO_BASE")).isPresent());
    }
}
