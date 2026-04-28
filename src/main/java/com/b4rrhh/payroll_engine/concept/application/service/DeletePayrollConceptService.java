package com.b4rrhh.payroll_engine.concept.application.service;

import com.b4rrhh.payroll_engine.concept.application.usecase.DeletePayrollConceptUseCase;
import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptNotFoundException;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeletePayrollConceptService implements DeletePayrollConceptUseCase {

    private final PayrollConceptRepository conceptRepository;

    public DeletePayrollConceptService(PayrollConceptRepository conceptRepository) {
        this.conceptRepository = conceptRepository;
    }

    @Override
    @Transactional
    public void delete(String ruleSystemCode, String conceptCode) {
        if (!conceptRepository.existsByBusinessKey(ruleSystemCode, conceptCode)) {
            throw new PayrollConceptNotFoundException(ruleSystemCode, conceptCode);
        }
        conceptRepository.deleteByBusinessKey(ruleSystemCode, conceptCode);
    }
}
