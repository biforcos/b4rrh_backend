package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptResultException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateByQuantityOperandResolverTest {

    private static final String RS = "ESP";
    private static final String TARGET_CODE = "SALARIO_BASE";

    private static PayrollObject obj(long id, String code) {
        return new PayrollObject(id, RS, PayrollObjectTypeCode.CONCEPT, code,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static PayrollConceptOperand operand(PayrollObject target, OperandRole role, PayrollObject source) {
        return new PayrollConceptOperand(null, target, role, source,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static SegmentExecutionState stateWith(String code, BigDecimal amount) {
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(new ConceptNodeIdentity(RS, code), amount);
        return state;
    }

    private static SegmentExecutionState stateWithBoth(String qCode, BigDecimal q, String rCode, BigDecimal r) {
        SegmentExecutionState state = new SegmentExecutionState();
        state.storeResult(new ConceptNodeIdentity(RS, qCode), q);
        state.storeResult(new ConceptNodeIdentity(RS, rCode), r);
        return state;
    }

    private static PayrollConceptOperandRepository fixedRepo(List<PayrollConceptOperand> operands) {
        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                return operands;
            }
        };
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void resolvesQuantityTimesRate() {
        PayrollObject targetObj = obj(3L, TARGET_CODE);
        PayrollObject qObj = obj(1L, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject rObj = obj(2L, "T_PRECIO_DIA");

        RateByQuantityOperandResolver resolver = new RateByQuantityOperandResolver(fixedRepo(List.of(
                operand(targetObj, OperandRole.QUANTITY, qObj),
                operand(targetObj, OperandRole.RATE, rObj)
        )), new RateByQuantityConfigurationValidator());

        SegmentExecutionState state = stateWithBoth(
                "T_DIAS_PRESENCIA_SEGMENTO", new BigDecimal("14"),
                "T_PRECIO_DIA", new BigDecimal("66.66666667")
        );

        Set<ConceptNodeIdentity> graphDeps = Set.of(
                new ConceptNodeIdentity(RS, "T_DIAS_PRESENCIA_SEGMENTO"),
                new ConceptNodeIdentity(RS, "T_PRECIO_DIA"));

        BigDecimal result = resolver.resolve(RS, TARGET_CODE, state, graphDeps);
        assertEquals(0, new BigDecimal("933.33").compareTo(result));
    }

    // ── missing operand definition ────────────────────────────────────────────

    @Test
    void throwsMissingOperandWhenQuantityIsAbsent() {
        PayrollObject targetObj = obj(3L, TARGET_CODE);
        PayrollObject rObj = obj(2L, "T_PRECIO_DIA");

        RateByQuantityOperandResolver resolver = new RateByQuantityOperandResolver(fixedRepo(List.of(
                operand(targetObj, OperandRole.RATE, rObj)
        )), new RateByQuantityConfigurationValidator());

        SegmentExecutionState state = stateWith("T_PRECIO_DIA", new BigDecimal("66.66666667"));
        Set<ConceptNodeIdentity> graphDeps = Set.of(new ConceptNodeIdentity(RS, "T_PRECIO_DIA"));
        assertThrows(MissingOperandDefinitionException.class,
                () -> resolver.resolve(RS, TARGET_CODE, state, graphDeps));
    }

    @Test
    void throwsMissingOperandWhenRateIsAbsent() {
        PayrollObject targetObj = obj(3L, TARGET_CODE);
        PayrollObject qObj = obj(1L, "T_DIAS_PRESENCIA_SEGMENTO");

        RateByQuantityOperandResolver resolver = new RateByQuantityOperandResolver(fixedRepo(List.of(
                operand(targetObj, OperandRole.QUANTITY, qObj)
        )), new RateByQuantityConfigurationValidator());

        SegmentExecutionState state = stateWith("T_DIAS_PRESENCIA_SEGMENTO", new BigDecimal("14"));
        Set<ConceptNodeIdentity> graphDeps = Set.of(new ConceptNodeIdentity(RS, "T_DIAS_PRESENCIA_SEGMENTO"));
        assertThrows(MissingOperandDefinitionException.class,
                () -> resolver.resolve(RS, TARGET_CODE, state, graphDeps));
    }

    @Test
    void throwsMissingOperandWhenNoOperantsExist() {
        RateByQuantityOperandResolver resolver = new RateByQuantityOperandResolver(
                fixedRepo(Collections.emptyList()), new RateByQuantityConfigurationValidator());

        assertThrows(MissingOperandDefinitionException.class,
                () -> resolver.resolve(RS, TARGET_CODE, new SegmentExecutionState(), Set.of()));
    }

    // ── duplicate operand definition ──────────────────────────────────────────

    @Test
    void throwsDuplicateWhenQuantityDefinedTwice() {
        PayrollObject targetObj = obj(3L, TARGET_CODE);
        PayrollObject q1 = obj(1L, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject q2 = obj(4L, "T_OTHER_QUANTITY");
        PayrollObject rObj = obj(2L, "T_PRECIO_DIA");

        RateByQuantityOperandResolver resolver = new RateByQuantityOperandResolver(fixedRepo(List.of(
                operand(targetObj, OperandRole.QUANTITY, q1),
                operand(targetObj, OperandRole.QUANTITY, q2),
                operand(targetObj, OperandRole.RATE, rObj)
        )), new RateByQuantityConfigurationValidator());

        SegmentExecutionState state = stateWithBoth(
                "T_DIAS_PRESENCIA_SEGMENTO", new BigDecimal("14"),
                "T_PRECIO_DIA", new BigDecimal("66.66666667")
        );
        Set<ConceptNodeIdentity> graphDeps = Set.of(
                new ConceptNodeIdentity(RS, "T_DIAS_PRESENCIA_SEGMENTO"),
                new ConceptNodeIdentity(RS, "T_OTHER_QUANTITY"),
                new ConceptNodeIdentity(RS, "T_PRECIO_DIA"));
        assertThrows(DuplicateOperandDefinitionException.class,
                () -> resolver.resolve(RS, TARGET_CODE, state, graphDeps));
    }

    // ── missing state amount ──────────────────────────────────────────────────

    @Test
    void throwsMissingConceptResultWhenQuantityNotInState() {
        PayrollObject targetObj = obj(3L, TARGET_CODE);
        PayrollObject qObj = obj(1L, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollObject rObj = obj(2L, "T_PRECIO_DIA");

        RateByQuantityOperandResolver resolver = new RateByQuantityOperandResolver(fixedRepo(List.of(
                operand(targetObj, OperandRole.QUANTITY, qObj),
                operand(targetObj, OperandRole.RATE, rObj)
        )), new RateByQuantityConfigurationValidator());

        // only rate is in state, quantity is missing
        SegmentExecutionState state = stateWith("T_PRECIO_DIA", new BigDecimal("66.66666667"));
        Set<ConceptNodeIdentity> graphDeps = Set.of(
                new ConceptNodeIdentity(RS, "T_DIAS_PRESENCIA_SEGMENTO"),
                new ConceptNodeIdentity(RS, "T_PRECIO_DIA"));
        assertThrows(MissingConceptResultException.class,
                () -> resolver.resolve(RS, TARGET_CODE, state, graphDeps));
    }
}
