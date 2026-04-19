package com.b4rrhh.payroll_engine.concept.domain.port;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;

import java.util.Optional;

public interface PayrollConceptRepository {

    PayrollConcept save(PayrollConcept concept);

    Optional<PayrollConcept> findByBusinessKey(String ruleSystemCode, String conceptCode);

    boolean existsByBusinessKey(String ruleSystemCode, String conceptCode);
}
