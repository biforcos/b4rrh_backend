package com.b4rrhh.payroll_engine.planning.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptFeedRelationRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
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
 *       <li>Fetch its active feed relations via {@link PayrollConceptFeedRelationRepository}
 *           (keyed by the concept's persisted object ID).</li>
 *       <li>For each relation, check the source's {@code ruleSystemCode}.
 *           Skip cross-rule-system sources.</li>
 *       <li>If the source concept code has not been loaded yet, load it from
 *           {@link PayrollConceptRepository}. Fail fast if not found.</li>
 *       <li>Enqueue the newly loaded source concept for its own expansion.</li>
 *     </ul>
 *   </li>
 *   <li>Concepts with a null object ID (not yet persisted) cannot have persisted feed
 *       relations; they are retained as nodes but skipped during relation lookup.</li>
 *   <li>Continue until the queue is empty. Return all loaded concepts in discovery order.</li>
 * </ol>
 */
@Service
public class DefaultEligibleConceptExpansionService implements EligibleConceptExpansionService {

    private final PayrollConceptRepository conceptRepository;
    private final PayrollConceptFeedRelationRepository feedRelationRepository;

    public DefaultEligibleConceptExpansionService(
            PayrollConceptRepository conceptRepository,
            PayrollConceptFeedRelationRepository feedRelationRepository
    ) {
        this.conceptRepository = conceptRepository;
        this.feedRelationRepository = feedRelationRepository;
    }

    @Override
    public List<PayrollConcept> expand(List<PayrollConcept> eligibleConcepts, LocalDate referenceDate) {
        // Ordered map: conceptCode → concept, preserving discovery order
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
            Long objectId = current.getObject().getId();
            if (objectId == null) {
                // Concept not persisted; no feed relations can exist in the repository.
                continue;
            }

            String ruleSystemCode = current.getRuleSystemCode();
            List<PayrollConceptFeedRelation> relations =
                    feedRelationRepository.findActiveByTargetObjectId(objectId, referenceDate);

            for (PayrollConceptFeedRelation relation : relations) {
                PayrollObject source = relation.getSourceObject();

                // Only expand within the same rule system.
                if (!ruleSystemCode.equals(source.getRuleSystemCode())) {
                    continue;
                }

                String sourceCode = source.getObjectCode();
                if (loaded.containsKey(sourceCode)) {
                    continue;
                }

                PayrollConcept sourceConcept = conceptRepository
                        .findByBusinessKey(ruleSystemCode, sourceCode)
                        .orElseThrow(() -> new MissingDependencyConceptDefinitionException(
                                ruleSystemCode, sourceCode));

                loaded.put(sourceCode, sourceConcept);
                toProcess.add(sourceConcept);
            }
        }

        return new ArrayList<>(loaded.values());
    }
}
