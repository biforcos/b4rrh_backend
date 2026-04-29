package com.b4rrhh.payroll_engine.concept.application.usecase;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;

public interface UpdateConceptSummaryUseCase {
    PayrollConcept update(UpdateConceptSummaryCommand command);
}
