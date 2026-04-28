package com.b4rrhh.payroll_engine.concept.application.usecase;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import java.util.List;

public interface ListPayrollConceptsUseCase {
    List<PayrollConcept> listByRuleSystemCode(String ruleSystemCode);
}
