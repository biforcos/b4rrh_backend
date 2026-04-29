package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptFeedRelationRepository;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateAggregateSourceException;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateConceptIdentityException;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.model.AggregateSourceEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
 *   <li>For {@code RATE_BY_QUANTITY} and {@code PERCENTAGE} concepts, additionally:
 *       <ul>
 *         <li>Load operand definitions from {@link PayrollConceptOperandRepository}.</li>
 *         <li>Validate graph ↔ operand coherence via {@link OperandConfigurationValidator}.</li>
 *         <li>Resolve exactly two operand source identities (QUANTITY+RATE for RATE_BY_QUANTITY;
 *             BASE+PERCENTAGE for PERCENTAGE).</li>
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

    private static final Logger log = LoggerFactory.getLogger(DefaultExecutionPlanBuilder.class);

    private final PayrollConceptOperandRepository operandRepository;
    private final OperandConfigurationValidator configurationValidator;
    private final PayrollConceptFeedRelationRepository feedRelationRepository;

    public DefaultExecutionPlanBuilder(
            PayrollConceptOperandRepository operandRepository,
            OperandConfigurationValidator configurationValidator,
            PayrollConceptFeedRelationRepository feedRelationRepository
    ) {
        this.operandRepository = operandRepository;
        this.configurationValidator = configurationValidator;
        this.feedRelationRepository = feedRelationRepository;
    }

    @Override
    public List<ConceptExecutionPlanEntry> build(ConceptDependencyGraph graph, List<PayrollConcept> concepts, LocalDate referenceDate) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null");
        }
        if (concepts == null) {
            throw new IllegalArgumentException("concepts must not be null");
        }
        Map<ConceptNodeIdentity, PayrollConcept> conceptIndex = buildIndex(concepts);
        List<ConceptNodeIdentity> orderedNodes = graph.topologicalOrder();

        log.debug("[ENGINE]   PLAN | orden topológico → [{}]",
                orderedNodes.stream().map(ConceptNodeIdentity::getConceptCode)
                        .collect(java.util.stream.Collectors.joining(" → ")));

        List<ConceptExecutionPlanEntry> plan = new ArrayList<>(orderedNodes.size());

        for (ConceptNodeIdentity identity : orderedNodes) {
            PayrollConcept concept = conceptIndex.get(identity);
            if (concept == null) {
                throw new MissingConceptDefinitionException(identity);
            }
            plan.add(buildEntry(graph, identity, concept, referenceDate));
        }

        return plan;
    }

    /**
     * Builds a single plan entry. For RATE_BY_QUANTITY and PERCENTAGE, loads and validates
     * operand definitions and embeds them into the entry. For AGGREGATE, resolves the source
     * concept identities from the dependency graph, enriches each with its invertSign flag
     * from the active feed relations, and embeds them into the entry.
     */
    private ConceptExecutionPlanEntry buildEntry(
            ConceptDependencyGraph graph,
            ConceptNodeIdentity identity,
            PayrollConcept concept,
            LocalDate referenceDate
    ) {
        CalculationType calculationType = concept.getCalculationType();

        if (calculationType == CalculationType.AGGREGATE) {
            Set<ConceptNodeIdentity> graphDeps = graph.getDependenciesOf(identity);
            if (graphDeps.isEmpty()) {
                log.debug("[ENGINE]     PLAN {} | AGGREGATE sin fuentes → resultado 0", identity.getConceptCode());
                return new ConceptExecutionPlanEntry(identity, calculationType, Map.of(), List.of());
            }

            Long targetObjectId = concept.getObject().getId();
            List<PayrollConceptFeedRelation> feedRelations =
                    feedRelationRepository.findActiveByTargetObjectId(targetObjectId, referenceDate);

            Map<String, Boolean> invertSignBySourceCode = feedRelations.stream()
                    .collect(Collectors.toMap(
                            r -> r.getSourceObject().getObjectCode(),
                            PayrollConceptFeedRelation::isInvertSign
                    ));

            Set<ConceptNodeIdentity> seen = new HashSet<>();
            List<AggregateSourceEntry> sources = new ArrayList<>();
            for (ConceptNodeIdentity source : graphDeps) {
                if (!seen.add(source)) {
                    throw new DuplicateAggregateSourceException(identity, source);
                }
                boolean invertSign = invertSignBySourceCode.getOrDefault(source.getConceptCode(), false);
                sources.add(new AggregateSourceEntry(source, invertSign));
            }

            log.debug("[ENGINE]     PLAN {} | AGGREGATE ← fuentes=[{}]",
                    identity.getConceptCode(),
                    sources.stream()
                            .map(s -> s.identity().getConceptCode() + (s.invertSign() ? "(−)" : "(+)"))
                            .collect(java.util.stream.Collectors.joining(", ")));

            return new ConceptExecutionPlanEntry(
                    identity,
                    calculationType,
                    Map.of(),
                    List.copyOf(sources)
            );
        }

        if (calculationType == CalculationType.RATE_BY_QUANTITY) {
            ConceptExecutionPlanEntry entry = buildOperandWiredEntry(graph, identity, calculationType,
                    OperandRole.QUANTITY, OperandRole.RATE);
            log.debug("[ENGINE]     PLAN {} | RATE_BY_QUANTITY: QUANTITY={} RATE={}",
                    identity.getConceptCode(),
                    entry.operands().get(OperandRole.QUANTITY).getConceptCode(),
                    entry.operands().get(OperandRole.RATE).getConceptCode());
            return entry;
        }

        if (calculationType == CalculationType.PERCENTAGE) {
            ConceptExecutionPlanEntry entry = buildOperandWiredEntry(graph, identity, calculationType,
                    OperandRole.BASE, OperandRole.PERCENTAGE);
            log.debug("[ENGINE]     PLAN {} | PERCENTAGE: BASE={} PCT={}",
                    identity.getConceptCode(),
                    entry.operands().get(OperandRole.BASE).getConceptCode(),
                    entry.operands().get(OperandRole.PERCENTAGE).getConceptCode());
            return entry;
        }

        log.debug("[ENGINE]     PLAN {} | {} (sin operandos)", identity.getConceptCode(), calculationType);
        return new ConceptExecutionPlanEntry(identity, calculationType);
    }

    /**
     * Shared helper for calculation types that require exactly two persisted operand roles
     * ({@code RATE_BY_QUANTITY} uses QUANTITY+RATE; {@code PERCENTAGE} uses BASE+PERCENTAGE).
     *
     * <p>Loads operand definitions from the repository, validates graph coherence, resolves
     * one source identity per role, and returns an enriched plan entry.
     */
    private ConceptExecutionPlanEntry buildOperandWiredEntry(
            ConceptDependencyGraph graph,
            ConceptNodeIdentity identity,
            CalculationType calculationType,
            OperandRole role1,
            OperandRole role2
    ) {
        String ruleSystemCode = identity.getRuleSystemCode();
        String conceptCode    = identity.getConceptCode();

        List<PayrollConceptOperand> operands  = operandRepository.findByTarget(ruleSystemCode, conceptCode);
        Set<ConceptNodeIdentity> declaredDeps = graph.getDependenciesOf(identity);

        configurationValidator.validate(ruleSystemCode, conceptCode, operands, declaredDeps);

        PayrollConceptOperand def1 = findSingle(operands, role1, ruleSystemCode, conceptCode);
        PayrollConceptOperand def2 = findSingle(operands, role2, ruleSystemCode, conceptCode);

        ConceptNodeIdentity id1 = new ConceptNodeIdentity(
                ruleSystemCode, def1.getSourceObject().getObjectCode());
        ConceptNodeIdentity id2 = new ConceptNodeIdentity(
                ruleSystemCode, def2.getSourceObject().getObjectCode());

        return new ConceptExecutionPlanEntry(
                identity,
                calculationType,
                Map.of(role1, id1, role2, id2)
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
