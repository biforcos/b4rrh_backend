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

class GreatestConceptResolverTest {

    private static final String RS = "ESP";

    private final GreatestConceptResolver resolver = new GreatestConceptResolver();

    private static ConceptNodeIdentity id(String code) {
        return new ConceptNodeIdentity(RS, code);
    }

    private static ConceptExecutionPlanEntry entry(String target, String leftCode, String rightCode) {
        return new ConceptExecutionPlanEntry(
                id(target),
                CalculationType.GREATEST,
                Map.of(OperandRole.LEFT, id(leftCode), OperandRole.RIGHT, id(rightCode))
        );
    }

    private static SegmentExecutionState stateWith(String leftCode, BigDecimal left,
                                                    String rightCode, BigDecimal right) {
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(id(leftCode), left);
        state.storeResult(id(rightCode), right);
        return state;
    }

    // ── happy path ─────────────────────────────────────────────────────────

    @Test
    void returnsLeftWhenLeftIsGreater() {
        ConceptExecutionPlanEntry e = entry("B_CC", "B01", "TOPE_MIN");
        SegmentExecutionState state = stateWith("B01", new BigDecimal("2500.00"), "TOPE_MIN", new BigDecimal("1260.00"));

        assertEquals(0, new BigDecimal("2500.00").compareTo(resolver.resolve(e, state)));
    }

    @Test
    void returnsRightWhenRightIsGreater() {
        ConceptExecutionPlanEntry e = entry("B_CC", "B01", "TOPE_MIN");
        SegmentExecutionState state = stateWith("B01", new BigDecimal("800.00"), "TOPE_MIN", new BigDecimal("1260.00"));

        assertEquals(0, new BigDecimal("1260.00").compareTo(resolver.resolve(e, state)));
    }

    @Test
    void returnsEitherWhenBothAreEqual() {
        ConceptExecutionPlanEntry e = entry("B_CC", "B01", "TOPE_MIN");
        SegmentExecutionState state = stateWith("B01", new BigDecimal("1260.00"), "TOPE_MIN", new BigDecimal("1260.00"));

        assertEquals(0, new BigDecimal("1260.00").compareTo(resolver.resolve(e, state)));
    }

    @Test
    void worksWithNegativeValues() {
        ConceptExecutionPlanEntry e = entry("R", "A", "B");
        SegmentExecutionState state = stateWith("A", new BigDecimal("-50.00"), "B", new BigDecimal("-200.00"));

        assertEquals(0, new BigDecimal("-50.00").compareTo(resolver.resolve(e, state)));
    }

    // ── fail-fast: missing planned operand ─────────────────────────────────

    @Test
    void missingLeftPlannedOperandThrows() {
        ConceptExecutionPlanEntry e = new ConceptExecutionPlanEntry(
                id("B_CC"), CalculationType.GREATEST,
                Map.of(OperandRole.RIGHT, id("TOPE_MIN"))
        );
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(id("TOPE_MIN"), new BigDecimal("1260.00"));

        assertThrows(MissingPlannedOperandException.class, () -> resolver.resolve(e, state));
    }

    @Test
    void missingRightPlannedOperandThrows() {
        ConceptExecutionPlanEntry e = new ConceptExecutionPlanEntry(
                id("B_CC"), CalculationType.GREATEST,
                Map.of(OperandRole.LEFT, id("B01"))
        );
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(id("B01"), new BigDecimal("2500.00"));

        assertThrows(MissingPlannedOperandException.class, () -> resolver.resolve(e, state));
    }

    // ── fail-fast: missing source amount in state ───────────────────────────

    @Test
    void missingLeftAmountInStateThrows() {
        ConceptExecutionPlanEntry e = entry("B_CC", "B01", "TOPE_MIN");
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(id("TOPE_MIN"), new BigDecimal("1260.00"));

        assertThrows(MissingConceptResultException.class, () -> resolver.resolve(e, state));
    }

    @Test
    void missingRightAmountInStateThrows() {
        ConceptExecutionPlanEntry e = entry("B_CC", "B01", "TOPE_MIN");
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(id("B01"), new BigDecimal("2500.00"));

        assertThrows(MissingConceptResultException.class, () -> resolver.resolve(e, state));
    }
}
