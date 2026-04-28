package com.b4rrhh.payroll_engine.concept.domain.port;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;

import java.time.LocalDate;
import java.util.List;

public interface PayrollConceptFeedRelationRepository {

    PayrollConceptFeedRelation save(PayrollConceptFeedRelation feedRelation);

    List<PayrollConceptFeedRelation> findActiveByTargetObjectId(Long targetObjectId, LocalDate referenceDate);

    /**
     * Returns every feed relation whose target concept matches the given business key,
     * ordered by source object code. Returns an empty list when no feed is configured.
     */
    List<PayrollConceptFeedRelation> findByRuleSystemCodeAndTargetConceptCode(
            String ruleSystemCode, String conceptCode);

    /**
     * Removes every feed relation whose target concept matches the given business key.
     * The operation is a no-op when no feed exists; it never raises.
     */
    void deleteAllByRuleSystemCodeAndTargetConceptCode(
            String ruleSystemCode, String conceptCode);
}
