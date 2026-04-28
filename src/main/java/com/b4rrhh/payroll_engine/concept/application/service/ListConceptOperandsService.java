package com.b4rrhh.payroll_engine.concept.application.service;

import com.b4rrhh.payroll_engine.concept.application.usecase.ListConceptOperandsUseCase;
import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptNotFoundException;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Returns the operands attached to a payroll concept identified by ({@code ruleSystemCode},
 * {@code conceptCode}). Throws {@link PayrollConceptNotFoundException} when the concept
 * does not exist; an existing concept with no operands resolves to an empty list.
 */
@Service
public class ListConceptOperandsService implements ListConceptOperandsUseCase {

    private final PayrollConceptRepository conceptRepository;
    private final PayrollConceptOperandRepository operandRepository;

    public ListConceptOperandsService(
            PayrollConceptRepository conceptRepository,
            PayrollConceptOperandRepository operandRepository
    ) {
        this.conceptRepository = conceptRepository;
        this.operandRepository = operandRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollConceptOperand> list(String ruleSystemCode, String conceptCode) {
        if (!conceptRepository.existsByBusinessKey(ruleSystemCode, conceptCode)) {
            throw new PayrollConceptNotFoundException(ruleSystemCode, conceptCode);
        }
        return operandRepository.findByRuleSystemCodeAndConceptCode(ruleSystemCode, conceptCode);
    }
}
