package com.b4rrhh.payroll_engine.concept.application.service;

import com.b4rrhh.payroll_engine.concept.application.usecase.ListPayrollConceptsUseCase;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListPayrollConceptsService implements ListPayrollConceptsUseCase {

    private final PayrollConceptRepository conceptRepository;

    public ListPayrollConceptsService(PayrollConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollConcept> listByRuleSystemCode(String ruleSystemCode) {
        return conceptRepository.findAllByRuleSystemCode(ruleSystemCode);
    }
}
