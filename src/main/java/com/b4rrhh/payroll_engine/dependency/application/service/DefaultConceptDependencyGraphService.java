package com.b4rrhh.payroll_engine.dependency.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptFeedRelationRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraphBuilder;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ConceptDependencyGraphService}.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>Add all input concepts as nodes.</li>
 *   <li>Build a set of known node identities for source filtering.</li>
 *   <li>For each concept (target), query active feed relations by technical object ID
 *       and reference date — one repository call per target concept. Only relations
 *       whose source is a CONCEPT within the known set are added as FEED_DEPENDENCY edges.</li>
 *   <li>For each RATE_BY_QUANTITY or PERCENTAGE concept, load its operand definitions
 *       and add OPERAND_DEPENDENCY edges for each source concept in the known set.
 *       This ensures topological ordering places operand sources before their dependents
 *       even when operand relationships are not encoded as feed relations.</li>
 *   <li>Build and validate the graph (cycle detection).</li>
 * </ol>
 */
@Service
public class DefaultConceptDependencyGraphService implements ConceptDependencyGraphService {

    private final PayrollConceptFeedRelationRepository feedRelationRepository;
    private final PayrollConceptOperandRepository operandRepository;

    public DefaultConceptDependencyGraphService(
            PayrollConceptFeedRelationRepository feedRelationRepository,
            PayrollConceptOperandRepository operandRepository
    ) {
        this.feedRelationRepository = feedRelationRepository;
        this.operandRepository = operandRepository;
    }

    @Override
    public ConceptDependencyGraph build(List<PayrollConcept> concepts, LocalDate referenceDate) {
        if (concepts == null) {
            throw new IllegalArgumentException("concepts must not be null");
        }
        if (referenceDate == null) {
            throw new IllegalArgumentException("referenceDate must not be null");
        }

        Set<ConceptNodeIdentity> knownIdentities = new HashSet<>();
        for (PayrollConcept concept : concepts) {
            knownIdentities.add(new ConceptNodeIdentity(concept.getRuleSystemCode(), concept.getConceptCode()));
        }

        Map<String, PayrollConcept> conceptByCode = concepts.stream()
                .collect(Collectors.toMap(PayrollConcept::getConceptCode, c -> c));

        ConceptDependencyGraphBuilder builder = new ConceptDependencyGraphBuilder()
                .addNodes(concepts);

        for (PayrollConcept target : concepts) {
            Long targetId = target.getObject().getId();
            if (targetId != null) {
                List<PayrollConceptFeedRelation> relations =
                        feedRelationRepository.findActiveByTargetObjectId(targetId, referenceDate);

                for (PayrollConceptFeedRelation relation : relations) {
                    ConceptNodeIdentity sourceIdentity = new ConceptNodeIdentity(
                            relation.getSourceObject().getRuleSystemCode(),
                            relation.getSourceObject().getObjectCode()
                    );
                    if (knownIdentities.contains(sourceIdentity)) {
                        builder.addFeedRelation(relation);
                    }
                }
            }

            CalculationType calcType = target.getCalculationType();
            if (calcType == CalculationType.RATE_BY_QUANTITY || calcType == CalculationType.PERCENTAGE) {
                List<PayrollConceptOperand> operands =
                        operandRepository.findByTarget(target.getRuleSystemCode(), target.getConceptCode());

                for (PayrollConceptOperand operand : operands) {
                    String sourceCode = operand.getSourceObject().getObjectCode();
                    ConceptNodeIdentity sourceIdentity =
                            new ConceptNodeIdentity(target.getRuleSystemCode(), sourceCode);
                    if (knownIdentities.contains(sourceIdentity)) {
                        PayrollConcept sourceConcept = conceptByCode.get(sourceCode);
                        builder.addOperandDependency(target, sourceConcept);
                    }
                }
            }
        }

        return builder.build();
    }
}
