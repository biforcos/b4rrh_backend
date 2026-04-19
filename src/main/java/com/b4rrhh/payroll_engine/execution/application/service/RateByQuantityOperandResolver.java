package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.OperandGraphMismatchException;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

/**
 * Resolves the result of a {@code RATE_BY_QUANTITY} execution by reading operand
 * definitions from {@link PayrollConceptOperandRepository}, validating coherence with the
 * concept dependency graph, and combining pre-computed amounts from execution state.
 *
 * <p>Expects exactly one operand per role ({@link OperandRole#QUANTITY} and
 * {@link OperandRole#RATE}) for the given target concept.
 *
 * <h3>Graph ↔ operand coherence</h3>
 * <p>Before reading amounts from state, this resolver calls
 * {@link RateByQuantityConfigurationValidator} to assert that each configured operand source
 * is a declared dependency in the graph. This guards against silent miscalculations that
 * would occur if operand configuration and graph structure drift out of sync.
 *
 * <h3>Runtime limitation — operand lookup frequency</h3>
 * <p>Operand definitions are currently loaded from the repository on every call to
 * {@link #resolve}, i.e. once per RATE_BY_QUANTITY concept per segment. This is acceptable
 * for the current PoC but is not the intended optimized runtime model.
 * Future iterations should preload operand configuration into an in-memory structure at
 * plan-construction time. Per-segment execution should then operate entirely in-memory,
 * with no additional repository access beyond the initial load.
 *
 * <h3>Fail-fast contract</h3>
 * <p>This resolver relies on the execution plan being topologically ordered. If a required
 * source concept has not yet been computed (i.e. its value is absent from
 * {@link SegmentExecutionState}), {@link com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptResultException}
 * is thrown immediately via {@link SegmentExecutionState#getRequiredAmount}. This fail-fast
 * behavior ensures that missing dependencies are surfaced immediately rather than propagating
 * silently as null or zero values.
 */
@Component
public class RateByQuantityOperandResolver {

    private static final int AMOUNT_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final PayrollConceptOperandRepository operandRepository;
    private final RateByQuantityConfigurationValidator configurationValidator;

    public RateByQuantityOperandResolver(
            PayrollConceptOperandRepository operandRepository,
            RateByQuantityConfigurationValidator configurationValidator
    ) {
        this.operandRepository = operandRepository;
        this.configurationValidator = configurationValidator;
    }

    /**
     * Computes quantity × rate for the given target concept.
     *
     * <ol>
     *   <li>Load operand definitions from repository.</li>
     *   <li>Validate that each operand source is a declared graph dependency
     *       ({@link OperandGraphMismatchException} if not).</li>
     *   <li>Read source amounts from {@code state} (fail-fast if absent).</li>
     *   <li>Return quantity × rate, rounded to scale 2 HALF_UP.</li>
     * </ol>
     *
     * @param ruleSystemCode   rule system of the target concept
     * @param conceptCode      code of the target concept
     * @param state            current segment execution state (must contain source amounts)
     * @param graphDependencies direct graph dependencies of the target concept — used for
     *                          coherence validation; obtained from
     *                          {@link com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph#getDependenciesOf}
     * @throws MissingOperandDefinitionException  if a required role is not configured
     * @throws DuplicateOperandDefinitionException if more than one operand is configured per role
     * @throws OperandGraphMismatchException       if a configured operand source is absent from
     *                                             the graph dependencies
     */
    public BigDecimal resolve(
            String ruleSystemCode,
            String conceptCode,
            SegmentExecutionState state,
            Set<ConceptNodeIdentity> graphDependencies
    ) {
        List<PayrollConceptOperand> operands = operandRepository.findByTarget(ruleSystemCode, conceptCode);

        configurationValidator.validate(ruleSystemCode, conceptCode, operands, graphDependencies);

        PayrollConceptOperand quantityDef = findSingle(operands, OperandRole.QUANTITY, ruleSystemCode, conceptCode);
        PayrollConceptOperand rateDef = findSingle(operands, OperandRole.RATE, ruleSystemCode, conceptCode);

        ConceptNodeIdentity quantityId = new ConceptNodeIdentity(
                ruleSystemCode, quantityDef.getSourceObject().getObjectCode());
        ConceptNodeIdentity rateId = new ConceptNodeIdentity(
                ruleSystemCode, rateDef.getSourceObject().getObjectCode());

        BigDecimal quantity = state.getRequiredAmount(quantityId);
        BigDecimal rate = state.getRequiredAmount(rateId);

        return quantity.multiply(rate).setScale(AMOUNT_SCALE, ROUNDING);
    }

    private PayrollConceptOperand findSingle(
            List<PayrollConceptOperand> operands,
            OperandRole role,
            String ruleSystemCode,
            String conceptCode
    ) {
        List<PayrollConceptOperand> matching = operands.stream()
                .filter(o -> o.getOperandRole() == role)
                .toList();
        if (matching.isEmpty()) {
            throw new MissingOperandDefinitionException(ruleSystemCode, conceptCode, role);
        }
        if (matching.size() > 1) {
            throw new DuplicateOperandDefinitionException(ruleSystemCode, conceptCode, role);
        }
        return matching.get(0);
    }
}
