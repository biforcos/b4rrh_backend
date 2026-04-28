package com.b4rrhh.payroll_engine.concept.application.usecase;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;

import java.util.List;

public interface ReplaceConceptOperandsUseCase {

    List<PayrollConceptOperand> replace(ReplaceConceptOperandsCommand command);
}
