package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.OperandGraphMismatchException;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link RateByQuantityConfigurationValidator}.
 *
 * <p>Validates graph ↔ operand coherence: every operand source must be declared
 * as a dependency of the target concept in the graph.
 */
class RateByQuantityConfigurationValidatorTest {

    private static final String RS = "ESP";
    private static final String TARGET = "SALARIO_BASE";

    private final RateByQuantityConfigurationValidator validator = new RateByQuantityConfigurationValidator();

    private static PayrollObject obj(long id, String code) {
        return new PayrollObject(id, RS, PayrollObjectTypeCode.CONCEPT, code, LocalDateTime.now(), LocalDateTime.now());
    }

    private static PayrollConceptOperand operand(OperandRole role, String sourceCode) {
        return new PayrollConceptOperand(null, obj(99L, TARGET), role, obj(1L, sourceCode),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static ConceptNodeIdentity id(String code) {
        return new ConceptNodeIdentity(RS, code);
    }

    // ── happy path ──────────────────────────────────────────────────────────

    @Test
    void validWhenBothOperandSourcesAreInGraphDeps() {
        List<PayrollConceptOperand> operands = List.of(
                operand(OperandRole.QUANTITY, "T_DIAS_PRESENCIA_SEGMENTO"),
                operand(OperandRole.RATE, "T_PRECIO_DIA")
        );
        Set<ConceptNodeIdentity> graphDeps = Set.of(
                id("T_DIAS_PRESENCIA_SEGMENTO"),
                id("T_PRECIO_DIA")
        );

        assertDoesNotThrow(() -> validator.validate(RS, TARGET, operands, graphDeps));
    }

    @Test
    void validWhenNoOperandsDefined() {
        assertDoesNotThrow(() -> validator.validate(RS, TARGET, List.of(), Set.of()));
    }

    // ── mismatch cases ───────────────────────────────────────────────────────

    @Test
    void throwsWhenQuantitySourceNotInGraphDeps() {
        List<PayrollConceptOperand> operands = List.of(
                operand(OperandRole.QUANTITY, "T_DIAS_PRESENCIA_SEGMENTO"),
                operand(OperandRole.RATE, "T_PRECIO_DIA")
        );
        // graph only declares T_PRECIO_DIA as a dependency — T_DIAS_PRESENCIA_SEGMENTO is missing
        Set<ConceptNodeIdentity> graphDeps = Set.of(id("T_PRECIO_DIA"));

        assertThrows(OperandGraphMismatchException.class,
                () -> validator.validate(RS, TARGET, operands, graphDeps));
    }

    @Test
    void throwsWhenRateSourceNotInGraphDeps() {
        List<PayrollConceptOperand> operands = List.of(
                operand(OperandRole.QUANTITY, "T_DIAS_PRESENCIA_SEGMENTO"),
                operand(OperandRole.RATE, "T_PRECIO_DIA")
        );
        // graph only declares T_DIAS_PRESENCIA_SEGMENTO — T_PRECIO_DIA is missing
        Set<ConceptNodeIdentity> graphDeps = Set.of(id("T_DIAS_PRESENCIA_SEGMENTO"));

        assertThrows(OperandGraphMismatchException.class,
                () -> validator.validate(RS, TARGET, operands, graphDeps));
    }

    @Test
    void throwsWhenGraphDepsIsEmpty() {
        List<PayrollConceptOperand> operands = List.of(
                operand(OperandRole.QUANTITY, "T_DIAS_PRESENCIA_SEGMENTO")
        );

        assertThrows(OperandGraphMismatchException.class,
                () -> validator.validate(RS, TARGET, operands, Set.of()));
    }
}
