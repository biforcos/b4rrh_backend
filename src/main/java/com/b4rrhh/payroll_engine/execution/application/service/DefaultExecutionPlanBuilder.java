package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateAggregateSourceException;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateConceptIdentityException;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingAggregateSourcesException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 *   <li>For {@code DIRECT_AMOUNT} concepts, produce a plain {@link ConceptExecutionPlanEntry}
 *       (no operand wiring needed).</li>
 *   <li>For {@code RATE_BY_QUANTITY} concepts, additionally:
 *       <ul>
 *         <li>Load operand definitions from {@link PayrollConceptOperandRepository}.</li>
 *         <li>Validate graph ↔ operand coherence via {@link RateByQuantityConfigurationValidator}.</li>
 *         <li>Resolve exactly one QUANTITY and one RATE operand source identity.</li>
 *         <li>Embed the operand wiring into the plan entry.</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h3>Why at plan-build time</h3>
 * <p>Loading and validating operand configuration once during plan construction ensures
 * that per-segment execution is fully in-memory. The segment engine never accesses the
 * repository; it reads operand source identities directly from the pre-enriched plan entry.
 */
@Component
public class DefaultExecutionPlanBuilder implements ExecutionPlanBuilder {

    private final PayrollConceptOperandRepository operandRepository;
    private final RateByQuantityConfigurationValidator configurationValidator;

    public DefaultExecutionPlanBuilder(
            PayrollConceptOperandRepository operandRepository,
            RateByQuantityConfigurationValidator configurationValidator
    ) {
        this.operandRepository = operandRepository;
        this.configurationValidator = configurationValidator;
    }

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
            plan.add(buildEntry(graph, identity, concept));
        }

        return plan;
    }

    /**
     * Builds a single plan entry. For RATE_BY_QUANTITY, loads and validates operand
     * definitions and embeds them into the entry. For AGGREGATE, resolves the source
     * concept identities from the dependency graph and embeds them into the entry.
     */
    private ConceptExecutionPlanEntry buildEntry(
            ConceptDependencyGraph graph,
            ConceptNodeIdentity identity,
            PayrollConcept concept
    ) {
        CalculationType calculationType = concept.getCalculationType();

        if (calculationType == CalculationType.AGGREGATE) {
            Set<ConceptNodeIdentity> graphDeps = graph.getDependenciesOf(identity);
            if (graphDeps.isEmpty()) {
                throw new MissingAggregateSourcesException(identity);
            }
            // The graph uses a Set, so structural duplicates are impossible.
            // Validate defensively against any future bypass of the graph.
            Set<ConceptNodeIdentity> seen = new HashSet<>();
            for (ConceptNodeIdentity source : graphDeps) {
                if (!seen.add(source)) {
                    throw new DuplicateAggregateSourceException(identity, source);
                }
            }
            return new ConceptExecutionPlanEntry(
                    identity,
                    calculationType,
                    Map.of(),
                    List.copyOf(graphDeps)
            );
        }

        if (calculationType != CalculationType.RATE_BY_QUANTITY) {
            return new ConceptExecutionPlanEntry(identity, calculationType);
        }

        String ruleSystemCode = identity.getRuleSystemCode();
        String conceptCode    = identity.getConceptCode();

        List<PayrollConceptOperand> operands  = operandRepository.findByTarget(ruleSystemCode, conceptCode);
        Set<ConceptNodeIdentity> declaredDeps = graph.getDependenciesOf(identity);

        configurationValidator.validate(ruleSystemCode, conceptCode, operands, declaredDeps);

        PayrollConceptOperand quantityDef = findSingle(operands, OperandRole.QUANTITY, ruleSystemCode, conceptCode);
        PayrollConceptOperand rateDef     = findSingle(operands, OperandRole.RATE,     ruleSystemCode, conceptCode);

        ConceptNodeIdentity quantityId = new ConceptNodeIdentity(
                ruleSystemCode, quantityDef.getSourceObject().getObjectCode());
        ConceptNodeIdentity rateId = new ConceptNodeIdentity(
                ruleSystemCode, rateDef.getSourceObject().getObjectCode());

        return new ConceptExecutionPlanEntry(
                identity,
                concept.getCalculationType(),
                Map.of(OperandRole.QUANTITY, quantityId, OperandRole.RATE, rateId)
        );
    }

    /**
     * Returns exactly one operand for the given role, or throws if zero or more than one exists.
     */
    private PayrollConceptOperand findSingle(
            List<PayrollConceptOperand> operands,
            OperandRole role,
            String ruleSystemCode,
            String conceptCode
    ) {
        List<PayrollConceptOperand> matching = operands.stream()
                .filter(o -> o.getOperandRole() == role)
                .toList();
        if (matching.isEmpty()) {
            throw new MissingOperandDefinitionException(ruleSystemCode, conceptCode, role);
        }
        if (matching.size() > 1) {
            throw new DuplicateOperandDefinitionException(ruleSystemCode, conceptCode, role);
        }
        return matching.get(0);
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
