package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateConceptIdentityException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ExecutionPlanBuilder}.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>Index the supplied concept list by {@code ConceptNodeIdentity(ruleSystemCode, conceptCode)}.
 *       Reject duplicates immediately.</li>
 *   <li>Obtain the topological order from {@code graph.topologicalOrder()}.
 *       This order guarantees that every dependency appears before its dependents.</li>
 *   <li>For each node in that order, look up the matching concept in the index.
 *       If no concept is found, fail fast with {@link MissingConceptDefinitionException}.</li>
 *   <li>Produce a {@link ConceptExecutionPlanEntry} carrying the node identity and
 *       the concept's {@code CalculationType}.</li>
 * </ol>
 *
 * <h3>Scope</h3>
 * This builder is purely structural. It does not evaluate applicability, filter by
 * employee type, or apply any runtime eligibility rules.
 */
@Component
public class DefaultExecutionPlanBuilder implements ExecutionPlanBuilder {

    @Override
    public List<ConceptExecutionPlanEntry> build(ConceptDependencyGraph graph, List<PayrollConcept> concepts) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null");
        }
        if (concepts == null) {
            throw new IllegalArgumentException("concepts must not be null");
        }
        Map<ConceptNodeIdentity, PayrollConcept> conceptIndex = buildIndex(concepts);
        List<ConceptNodeIdentity> orderedNodes = graph.topologicalOrder();
        List<ConceptExecutionPlanEntry> plan = new ArrayList<>(orderedNodes.size());

        for (ConceptNodeIdentity identity : orderedNodes) {
            PayrollConcept concept = conceptIndex.get(identity);
            if (concept == null) {
                throw new MissingConceptDefinitionException(identity);
            }
            plan.add(new ConceptExecutionPlanEntry(identity, concept.getCalculationType()));
        }

        return plan;
    }

    /**
     * Builds an index from {@code ConceptNodeIdentity} to {@code PayrollConcept}.
     * Rejects duplicate identities immediately.
     */
    private Map<ConceptNodeIdentity, PayrollConcept> buildIndex(List<PayrollConcept> concepts) {
        Map<ConceptNodeIdentity, PayrollConcept> index = new HashMap<>(concepts.size() * 2);
        for (PayrollConcept concept : concepts) {
            ConceptNodeIdentity identity = new ConceptNodeIdentity(
                    concept.getRuleSystemCode(),
                    concept.getConceptCode()
            );
            if (index.containsKey(identity)) {
                throw new DuplicateConceptIdentityException(identity);
            }
            index.put(identity, concept);
        }
        return index;
    }
}
