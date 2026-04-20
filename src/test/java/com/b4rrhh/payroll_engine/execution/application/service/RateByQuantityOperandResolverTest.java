package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptResultException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingPlannedOperandException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link RateByQuantityOperandResolver}.
 *
 * <p>The resolver reads pre-wired operand source identities from the plan entry
 * and amounts from execution state. No repository access occurs at runtime.
 */
class RateByQuantityOperandResolverTest {

    private static final String RS = "ESP";

    private final RateByQuantityOperandResolver resolver = new RateByQuantityOperandResolver();

    private static ConceptNodeIdentity node(String code) {
        return new ConceptNodeIdentity(RS, code);
    }

    private static ConceptExecutionPlanEntry enrichedEntry() {
        return new ConceptExecutionPlanEntry(
                node("SALARIO_BASE"),
                CalculationType.RATE_BY_QUANTITY,
                Map.of(
                        OperandRole.QUANTITY, node("T_DIAS_PRESENCIA_SEGMENTO"),
                        OperandRole.RATE,     node("T_PRECIO_DIA")
                )
        );
    }

    private static SegmentExecutionState stateWithBoth(BigDecimal quantity, BigDecimal rate) {
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(node("T_DIAS_PRESENCIA_SEGMENTO"), quantity);
        state.storeResult(node("T_PRECIO_DIA"), rate);
        return state;
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void resolvesQuantityTimesRate() {
        SegmentExecutionState state = stateWithBoth(
                new BigDecimal("14"),
                new BigDecimal("66.66666667")
        );

        BigDecimal result = resolver.resolve(enrichedEntry(), state);

        assertEquals(0, new BigDecimal("933.33").compareTo(result));
    }

    @Test
    void resultIsRoundedToScale2HalfUp() {
        // 10 * 0.555 = 5.55 (scale 8 product is 5.55000000 → scale 2 HALF_UP = 5.55)
        SegmentExecutionState state = stateWithBoth(
                new BigDecimal("10"),
                new BigDecimal("0.555")
        );

        BigDecimal result = resolver.resolve(enrichedEntry(), state);

        assertEquals(2, result.scale());
        assertEquals(0, new BigDecimal("5.55").compareTo(result));
    }

    // ── missing planned operand wiring ────────────────────────────────────────

    @Test
    void throwsMissingPlannedOperandWhenQuantityIsAbsent() {
        // Entry only has RATE wired, QUANTITY is absent from the plan entry's operands map
        ConceptExecutionPlanEntry entry = new ConceptExecutionPlanEntry(
                node("SALARIO_BASE"),
                CalculationType.RATE_BY_QUANTITY,
                Map.of(OperandRole.RATE, node("T_PRECIO_DIA"))
        );

        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(node("T_PRECIO_DIA"), new BigDecimal("66.66666667"));

        assertThrows(MissingPlannedOperandException.class, () -> resolver.resolve(entry, state));
    }

    @Test
    void throwsMissingPlannedOperandWhenRateIsAbsent() {
        // Entry only has QUANTITY wired, RATE is absent from the plan entry's operands map
        ConceptExecutionPlanEntry entry = new ConceptExecutionPlanEntry(
                node("SALARIO_BASE"),
                CalculationType.RATE_BY_QUANTITY,
                Map.of(OperandRole.QUANTITY, node("T_DIAS_PRESENCIA_SEGMENTO"))
        );

        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(node("T_DIAS_PRESENCIA_SEGMENTO"), new BigDecimal("14"));

        assertThrows(MissingPlannedOperandException.class, () -> resolver.resolve(entry, state));
    }

    @Test
    void throwsMissingPlannedOperandWhenOperandsMapIsEmpty() {
        // Entry has an empty operands map (2-arg constructor) — plan was not properly enriched
        ConceptExecutionPlanEntry entry = new ConceptExecutionPlanEntry(
                node("SALARIO_BASE"), CalculationType.RATE_BY_QUANTITY
        );

        assertThrows(MissingPlannedOperandException.class,
                () -> resolver.resolve(entry, new SegmentExecutionState()));
    }

    // ── missing state amount ──────────────────────────────────────────────────

    @Test
    void throwsMissingConceptResultWhenQuantityNotInState() {
        // Entry is fully wired but state only has RATE — QUANTITY is absent
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(node("T_PRECIO_DIA"), new BigDecimal("66.66666667"));

        assertThrows(MissingConceptResultException.class,
                () -> resolver.resolve(enrichedEntry(), state));
    }

    @Test
    void throwsMissingConceptResultWhenRateNotInState() {
        // Entry is fully wired but state only has QUANTITY — RATE is absent
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(node("T_DIAS_PRESENCIA_SEGMENTO"), new BigDecimal("14"));

        assertThrows(MissingConceptResultException.class,
                () -> resolver.resolve(enrichedEntry(), state));
    }
}
