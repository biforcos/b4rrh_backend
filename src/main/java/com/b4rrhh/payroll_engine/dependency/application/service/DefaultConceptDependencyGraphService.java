package com.b4rrhh.payroll_engine.dependency.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptFeedRelationRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraphBuilder;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link ConceptDependencyGraphService}.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>Add all input concepts as nodes.</li>
 *   <li>Build a set of known node identities for source filtering.</li>
 *   <li>For each concept (target), query active feed relations by technical object ID
 *       and reference date — one repository call per target concept.</li>
 *   <li>For each relation whose source is a known concept, add a FEED_DEPENDENCY edge:
 *       target depends on source.</li>
 *   <li>Build and validate the graph (cycle detection).</li>
 * </ol>
 *
 * <h3>Current design constraints</h3>
 * <p><strong>One lookup per target concept.</strong>
 * The current implementation performs one {@link PayrollConceptFeedRelationRepository}
 * call per concept in the input list. This is acceptable for the PoC scale, but should
 * be replaced by a bulk lookup once the concept set grows.
 *
 * <p><strong>Null-ID concepts are skipped.</strong>
 * Concepts whose {@link com.b4rrhh.payroll_engine.object.domain.model.PayrollObject} ID
 * is {@code null} have not been persisted and therefore cannot have persisted feed
 * relations. They are still added as graph nodes but no relation lookup is performed
 * for them.
 *
 * <p><strong>External-source relations are silently ignored.</strong>
 * When a feed relation's source concept is not present in the provided input concept set,
 * that relation is intentionally skipped and no node is created for the external source.
 * This is the correct behaviour for this iteration: the executor provides an explicit,
 * bounded list of concepts for the PoC. If a required source is absent, the execution
 * plan builder will fail explicitly when it cannot satisfy a dependency.
 */
@Service
public class DefaultConceptDependencyGraphService implements ConceptDependencyGraphService {

    private final PayrollConceptFeedRelationRepository feedRelationRepository;

    public DefaultConceptDependencyGraphService(PayrollConceptFeedRelationRepository feedRelationRepository) {
        this.feedRelationRepository = feedRelationRepository;
    }

    @Override
    public ConceptDependencyGraph build(List<PayrollConcept> concepts, LocalDate referenceDate) {
        if (concepts == null) {
            throw new IllegalArgumentException("concepts must not be null");
        }
        if (referenceDate == null) {
            throw new IllegalArgumentException("referenceDate must not be null");
        }

        // Index of known identities — used to filter out relations whose source
        // falls outside the provided concept set.
        Set<ConceptNodeIdentity> knownIdentities = new HashSet<>();
        for (PayrollConcept concept : concepts) {
            knownIdentities.add(new ConceptNodeIdentity(concept.getRuleSystemCode(), concept.getConceptCode()));
        }

        ConceptDependencyGraphBuilder builder = new ConceptDependencyGraphBuilder()
                .addNodes(concepts);

        for (PayrollConcept target : concepts) {
            Long targetId = target.getObject().getId();
            if (targetId == null) {
                // Concept is not yet persisted; no feed relations can exist in the repository.
                // The concept is still a valid graph node — it is already added via addNodes above.
                continue;
            }

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
                // Relations whose source is outside the input concept set are intentionally
                // ignored: no new node is introduced for the external source.
            }
        }

        return builder.build();
    }
}
