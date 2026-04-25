package com.b4rrhh.payroll_engine.planning.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptFeedRelationRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.planning.domain.exception.MissingDependencyConceptDefinitionException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Default implementation of {@link EligibleConceptExpansionService}.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>Seed the working queue with all input {@code eligibleConcepts}.</li>
 *   <li>For each concept dequeued:
 *     <ul>
 *       <li><strong>Operand discovery:</strong> for RATE_BY_QUANTITY and PERCENTAGE concepts,
 *           load operand definitions from {@link PayrollConceptOperandRepository} and enqueue
 *           each source concept that has not yet been loaded.</li>
 *       <li><strong>Feed-relation discovery:</strong> fetch active inbound feed relations via
 *           {@link PayrollConceptFeedRelationRepository}. Only CONCEPT-typed sources within
 *           the same rule system are followed; CONSTANT and TABLE sources are silently skipped
 *           because they are not concept definitions.</li>
 *     </ul>
 *   </li>
 *   <li>Continue until the queue is empty. Return all loaded concepts in discovery order.</li>
 * </ol>
 */
@Service
public class DefaultEligibleConceptExpansionService implements EligibleConceptExpansionService {

    private final PayrollConceptRepository conceptRepository;
    private final PayrollConceptFeedRelationRepository feedRelationRepository;
    private final PayrollConceptOperandRepository operandRepository;

    public DefaultEligibleConceptExpansionService(
            PayrollConceptRepository conceptRepository,
            PayrollConceptFeedRelationRepository feedRelationRepository,
            PayrollConceptOperandRepository operandRepository
    ) {
        this.conceptRepository = conceptRepository;
        this.feedRelationRepository = feedRelationRepository;
        this.operandRepository = operandRepository;
    }

    @Override
    public List<PayrollConcept> expand(List<PayrollConcept> eligibleConcepts, LocalDate referenceDate) {
        Map<String, PayrollConcept> loaded = new LinkedHashMap<>();
        Queue<PayrollConcept> toProcess = new ArrayDeque<>();

        for (PayrollConcept concept : eligibleConcepts) {
            if (!loaded.containsKey(concept.getConceptCode())) {
                loaded.put(concept.getConceptCode(), concept);
                toProcess.add(concept);
            }
        }

        while (!toProcess.isEmpty()) {
            PayrollConcept current = toProcess.poll();
            String ruleSystemCode = current.getRuleSystemCode();

            // Operand-based discovery: ensures RATE_BY_QUANTITY / PERCENTAGE source concepts
            // are included even when they have no feed relations pointing to the current concept.
            List<PayrollConceptOperand> operands =
                    operandRepository.findByTarget(ruleSystemCode, current.getConceptCode());
            for (PayrollConceptOperand operand : operands) {
                String sourceCode = operand.getSourceObject().getObjectCode();
                if (!loaded.containsKey(sourceCode)) {
                    PayrollConcept sourceConcept = conceptRepository
                            .findByBusinessKey(ruleSystemCode, sourceCode)
                            .orElseThrow(() -> new MissingDependencyConceptDefinitionException(
                                    ruleSystemCode, sourceCode));
                    loaded.put(sourceCode, sourceConcept);
                    toProcess.add(sourceConcept);
                }
            }

            // Feed-relation-based discovery: only CONCEPT-typed sources are followed.
            // CONSTANT and TABLE sources feed values into concepts but are not concept
            // definitions themselves and cannot be expanded further.
            Long objectId = current.getObject().getId();
            if (objectId == null) {
                continue;
            }

            List<PayrollConceptFeedRelation> relations =
                    feedRelationRepository.findActiveByTargetObjectId(objectId, referenceDate);
            for (PayrollConceptFeedRelation relation : relations) {
                PayrollObject source = relation.getSourceObject();
                if (source.getObjectTypeCode() != PayrollObjectTypeCode.CONCEPT) {
                    continue;
                }
                if (!ruleSystemCode.equals(source.getRuleSystemCode())) {
                    continue;
                }
                String sourceCode = source.getObjectCode();
                if (!loaded.containsKey(sourceCode)) {
                    PayrollConcept sourceConcept = conceptRepository
                            .findByBusinessKey(ruleSystemCode, sourceCode)
                            .orElseThrow(() -> new MissingDependencyConceptDefinitionException(
                                    ruleSystemCode, sourceCode));
                    loaded.put(sourceCode, sourceConcept);
                    toProcess.add(sourceConcept);
                }
            }
        }

        return new ArrayList<>(loaded.values());
    }
}
