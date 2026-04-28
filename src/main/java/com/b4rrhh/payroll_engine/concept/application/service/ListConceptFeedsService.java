package com.b4rrhh.payroll_engine.concept.application.service;

import com.b4rrhh.payroll_engine.concept.application.usecase.ListConceptFeedsUseCase;
import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptNotFoundException;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptFeedRelationRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Returns the feed relations whose target is the concept identified by
 * ({@code ruleSystemCode}, {@code conceptCode}). Throws
 * {@link PayrollConceptNotFoundException} when the concept does not exist.
 */
@Service
public class ListConceptFeedsService implements ListConceptFeedsUseCase {

    private final PayrollConceptRepository conceptRepository;
    private final PayrollConceptFeedRelationRepository feedRelationRepository;

    public ListConceptFeedsService(
            PayrollConceptRepository conceptRepository,
            PayrollConceptFeedRelationRepository feedRelationRepository
    ) {
        this.conceptRepository = conceptRepository;
        this.feedRelationRepository = feedRelationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollConceptFeedRelation> list(String ruleSystemCode, String conceptCode) {
        if (!conceptRepository.existsByBusinessKey(ruleSystemCode, conceptCode)) {
            throw new PayrollConceptNotFoundException(ruleSystemCode, conceptCode);
        }
        return feedRelationRepository.findByRuleSystemCodeAndTargetConceptCode(
                ruleSystemCode, conceptCode);
    }
}
