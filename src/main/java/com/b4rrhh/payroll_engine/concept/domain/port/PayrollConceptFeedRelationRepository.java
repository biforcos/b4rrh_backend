package com.b4rrhh.payroll_engine.concept.domain.port;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;

import java.time.LocalDate;
import java.util.List;

public interface PayrollConceptFeedRelationRepository {

    PayrollConceptFeedRelation save(PayrollConceptFeedRelation feedRelation);

    List<PayrollConceptFeedRelation> findActiveByTargetObjectId(Long targetObjectId, LocalDate referenceDate);
}
