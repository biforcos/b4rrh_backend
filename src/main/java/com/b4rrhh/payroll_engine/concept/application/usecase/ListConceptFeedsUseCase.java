package com.b4rrhh.payroll_engine.concept.application.usecase;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;

import java.util.List;

public interface ListConceptFeedsUseCase {

    List<PayrollConceptFeedRelation> list(String ruleSystemCode, String conceptCode);
}
