package com.b4rrhh.payroll_engine.dependency.domain.model;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a ConceptDependencyGraph from a set of PayrollConcepts and their feed relations.
 *
 * <p>Edge direction: an edge from {@code dependent → dependency} means
 * {@code dependency} must be calculated <em>before</em> {@code dependent}.
 * In topological order, {@code dependency} will appear at a lower index than {@code dependent}.
 *
 * <p>Two dependency types are supported:
 * <ul>
 *   <li>OPERAND_DEPENDENCY: declared explicitly via {@link #addOperandDependency}.</li>
 *   <li>FEED_DEPENDENCY: derived from a {@link com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation}.
 *       The source feeds into the target; therefore the target depends on the source.</li>
 * </ul>
 *
 * <p>Cycle detection is triggered eagerly on {@link #build()}.
 * Cross-rule-system feed relations are rejected immediately.
 */
public final class ConceptDependencyGraphBuilder {

    private final Set<ConceptNodeIdentity> nodes = new LinkedHashSet<>();
    private final List<ConceptDependency> edges = new ArrayList<>();

    public ConceptDependencyGraphBuilder addNode(PayrollConcept concept) {
        nodes.add(toIdentity(concept));
        return this;
    }

    public ConceptDependencyGraphBuilder addNodes(List<PayrollConcept> concepts) {
        concepts.forEach(this::addNode);
        return this;
    }

    /**
     * Declares that {@code dependent} requires {@code dependency} as an operand.
     * {@code dependency} must be calculated before {@code dependent}.
     */
    public ConceptDependencyGraphBuilder addOperandDependency(
            PayrollConcept dependent,
            PayrollConcept dependency
    ) {
        nodes.add(toIdentity(dependent));
        nodes.add(toIdentity(dependency));
        edges.add(new ConceptDependency(
                toIdentity(dependent),
                toIdentity(dependency),
                DependencyType.OPERAND_DEPENDENCY
        ));
        return this;
    }

    /**
     * Derives a FEED_DEPENDENCY from a PayrollConceptFeedRelation.
     *
     * <p>The source concept feeds data into the target concept; therefore the target depends
     * on the source being calculated first. This translates to an edge:
     * {@code target (dependent) → source (dependency)}.
     *
     * <p>Both source and target must belong to the same rule system. Cross-rule-system
     * feed edges are not supported at this structural level.
     *
     * @throws IllegalArgumentException if source and target belong to different rule systems
     */
    public ConceptDependencyGraphBuilder addFeedRelation(PayrollConceptFeedRelation feedRelation) {
        String sourceRuleSystem = feedRelation.getSourceObject().getRuleSystemCode();
        String targetRuleSystem = feedRelation.getTargetObject().getRuleSystemCode();
        if (!sourceRuleSystem.equals(targetRuleSystem)) {
            throw new IllegalArgumentException(
                    "Feed relation source and target must belong to the same rule system. " +
                    "Got source=" + sourceRuleSystem + ", target=" + targetRuleSystem
            );
        }
        ConceptNodeIdentity source = new ConceptNodeIdentity(
                sourceRuleSystem,
                feedRelation.getSourceObject().getObjectCode()
        );
        ConceptNodeIdentity target = new ConceptNodeIdentity(
                targetRuleSystem,
                feedRelation.getTargetObject().getObjectCode()
        );
        nodes.add(source);
        nodes.add(target);
        // target (dependent) → source (dependency): source must execute before target
        edges.add(new ConceptDependency(target, source, DependencyType.FEED_DEPENDENCY));
        return this;
    }

    /**
     * Builds and validates the graph. Throws ConceptDependencyCycleException if a cycle exists.
     */
    public ConceptDependencyGraph build() {
        ConceptDependencyGraph graph = new ConceptDependencyGraph(nodes, edges);
        // trigger cycle detection eagerly
        graph.topologicalOrder();
        return graph;
    }

    private ConceptNodeIdentity toIdentity(PayrollConcept concept) {
        return new ConceptNodeIdentity(concept.getRuleSystemCode(), concept.getConceptCode());
    }
}
