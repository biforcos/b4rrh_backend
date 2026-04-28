package com.b4rrhh.payroll_engine.concept.application.service;

import com.b4rrhh.payroll_engine.concept.application.usecase.ReplaceConceptOperandsCommand;
import com.b4rrhh.payroll_engine.concept.application.usecase.ReplaceConceptOperandsUseCase;
import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptNotFoundException;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import com.b4rrhh.payroll_engine.object.domain.exception.PayrollObjectNotFoundException;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.object.domain.port.PayrollObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces the operand definition of a concept atomically: every existing operand row
 * for the target concept is removed and the supplied items are persisted in order.
 *
 * <p>Validation rules enforced by this service:
 * <ul>
 *   <li>The target concept must exist (404 otherwise).</li>
 *   <li>Every {@code sourceObjectCode} must resolve to an existing CONCEPT in the same
 *       rule system (the operand domain model itself enforces both target and source
 *       being CONCEPT-typed).</li>
 *   <li>Source and target cannot be the same concept (enforced by the domain model).</li>
 * </ul>
 */
@Service
public class ReplaceConceptOperandsService implements ReplaceConceptOperandsUseCase {

    private final PayrollConceptRepository conceptRepository;
    private final PayrollConceptOperandRepository operandRepository;
    private final PayrollObjectRepository objectRepository;

    public ReplaceConceptOperandsService(
            PayrollConceptRepository conceptRepository,
            PayrollConceptOperandRepository operandRepository,
            PayrollObjectRepository objectRepository
    ) {
        this.conceptRepository = conceptRepository;
        this.operandRepository = operandRepository;
        this.objectRepository = objectRepository;
    }

    @Override
    @Transactional
    public List<PayrollConceptOperand> replace(ReplaceConceptOperandsCommand command) {
        String ruleSystemCode = command.ruleSystemCode();
        String conceptCode = command.conceptCode();

        if (!conceptRepository.existsByBusinessKey(ruleSystemCode, conceptCode)) {
            throw new PayrollConceptNotFoundException(ruleSystemCode, conceptCode);
        }

        PayrollObject targetObject = objectRepository
                .findByBusinessKey(ruleSystemCode, PayrollObjectTypeCode.CONCEPT, conceptCode)
                .orElseThrow(() -> new PayrollObjectNotFoundException(
                        ruleSystemCode, PayrollObjectTypeCode.CONCEPT.name(), conceptCode));

        operandRepository.deleteAllByRuleSystemCodeAndConceptCode(ruleSystemCode, conceptCode);

        List<PayrollConceptOperand> persisted = new ArrayList<>();
        if (command.items() == null || command.items().isEmpty()) {
            return persisted;
        }

        LocalDateTime now = LocalDateTime.now();
        for (ReplaceConceptOperandsCommand.Item item : command.items()) {
            PayrollObject sourceObject = objectRepository
                    .findByBusinessKey(ruleSystemCode, PayrollObjectTypeCode.CONCEPT, item.sourceObjectCode())
                    .orElseThrow(() -> new PayrollObjectNotFoundException(
                            ruleSystemCode, PayrollObjectTypeCode.CONCEPT.name(), item.sourceObjectCode()));

            PayrollConceptOperand operand = new PayrollConceptOperand(
                    null,
                    targetObject,
                    item.operandRole(),
                    sourceObject,
                    now,
                    now
            );
            persisted.add(operandRepository.save(operand));
        }
        return persisted;
    }
}
