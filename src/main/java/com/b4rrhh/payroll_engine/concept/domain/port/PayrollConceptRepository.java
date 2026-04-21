package com.b4rrhh.payroll_engine.concept.domain.port;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PayrollConceptRepository {

    PayrollConcept save(PayrollConcept concept);

    Optional<PayrollConcept> findByBusinessKey(String ruleSystemCode, String conceptCode);

    boolean existsByBusinessKey(String ruleSystemCode, String conceptCode);

    /**
     * Loads all concepts matching the given rule system and concept codes in a single query.
     * Codes not present in the repository are silently absent from the result —
     * callers are responsible for detecting missing concepts.
     */
    List<PayrollConcept> findAllByCodes(String ruleSystemCode, Collection<String> conceptCodes);
}
