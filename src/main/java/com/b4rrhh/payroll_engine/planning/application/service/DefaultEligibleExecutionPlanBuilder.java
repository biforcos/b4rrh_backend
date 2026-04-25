package com.b4rrhh.payroll_engine.planning.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import com.b4rrhh.payroll_engine.dependency.application.service.ConceptDependencyGraphService;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.eligibility.application.service.ResolveApplicableConceptsUseCase;
import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ResolvedConceptAssignment;
import com.b4rrhh.payroll_engine.execution.application.service.ExecutionPlanBuilder;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.planning.domain.exception.MissingEligibleConceptDefinitionException;
import com.b4rrhh.payroll_engine.planning.domain.model.EligibleExecutionPlanResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link BuildEligibleExecutionPlanUseCase}.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li><strong>Resolve applicable assignments</strong> — calls {@link ResolveApplicableConceptsUseCase}
 *       to obtain one winning assignment per applicable concept code for the given context
 *       and reference date.</li>
 *   <li><strong>Load eligible concept definitions</strong> — loads the concept definitions
 *       for all resolved codes in a single query. Fails fast with
 *       {@link MissingEligibleConceptDefinitionException} if any code lacks a definition.</li>
 *   <li><strong>Expand dependencies</strong> — delegates to {@link EligibleConceptExpansionService}
 *       to iteratively discover and load all structural dependencies within the same rule system.
 *       Technical concepts (e.g. T_DIAS_PRESENCIA_SEGMENTO) are pulled in here, not via
 *       eligibility assignments.</li>
 *   <li><strong>Build dependency graph</strong> — calls {@link ConceptDependencyGraphService}
 *       over the full expanded concept set. At this point, all nodes are present, so no
 *       feed relation edge will be silently dropped.</li>
 *   <li><strong>Build execution plan</strong> — calls {@link ExecutionPlanBuilder} to produce
 *       the topologically ordered plan.</li>
 *   <li><strong>Return auditable result</strong> — wraps all intermediate layers into
 *       {@link EligibleExecutionPlanResult}.</li>
 * </ol>
 *
 * <h3>Design note</h3>
 * <p>Eligibility decides WHICH concepts apply from a business perspective.
 * Dependency expansion decides WHICH additional concepts are structurally required for calculation.
 * These concerns are intentionally kept separate.
 */
@Service
public class DefaultEligibleExecutionPlanBuilder implements BuildEligibleExecutionPlanUseCase {

    private final ResolveApplicableConceptsUseCase eligibilityResolver;
    private final PayrollConceptRepository conceptRepository;
    private final EligibleConceptExpansionService expansionService;
    private final ConceptDependencyGraphService graphService;
    private final ExecutionPlanBuilder planBuilder;

    public DefaultEligibleExecutionPlanBuilder(
            ResolveApplicableConceptsUseCase eligibilityResolver,
            PayrollConceptRepository conceptRepository,
            EligibleConceptExpansionService expansionService,
            ConceptDependencyGraphService graphService,
            ExecutionPlanBuilder planBuilder
    ) {
        this.eligibilityResolver = eligibilityResolver;
        this.conceptRepository = conceptRepository;
        this.expansionService = expansionService;
        this.graphService = graphService;
        this.planBuilder = planBuilder;
    }

    @Override
    public EligibleExecutionPlanResult build(EmployeeAssignmentContext context, LocalDate referenceDate) {
        // Step 1: resolve applicable assignments
        List<ResolvedConceptAssignment> applicableAssignments =
                eligibilityResolver.resolve(context, referenceDate);

        // Step 2: load eligible concept definitions
        Set<String> eligibleCodes = applicableAssignments.stream()
                .map(ResolvedConceptAssignment::getConceptCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<PayrollConcept> eligibleConcepts =
                conceptRepository.findAllByCodes(context.getRuleSystemCode(), eligibleCodes);

        // Fail fast if any eligible concept code has no matching definition
        Set<String> foundCodes = eligibleConcepts.stream()
                .map(PayrollConcept::getConceptCode)
                .collect(Collectors.toSet());
        for (String code : eligibleCodes) {
            if (!foundCodes.contains(code)) {
                throw new MissingEligibleConceptDefinitionException(context.getRuleSystemCode(), code);
            }
        }

        // Step 3: expand dependencies (iteratively discovers and loads all structural dependencies)
        List<PayrollConcept> expandedConcepts = expansionService.expand(eligibleConcepts, referenceDate);

        // Step 4: build dependency graph over the full expanded concept set
        ConceptDependencyGraph dependencyGraph = graphService.build(expandedConcepts, referenceDate);

        // Step 5: build ordered execution plan
        List<ConceptExecutionPlanEntry> executionPlan = planBuilder.build(dependencyGraph, expandedConcepts, referenceDate);

        return new EligibleExecutionPlanResult(
                applicableAssignments,
                List.copyOf(eligibleConcepts),
                List.copyOf(expandedConcepts),
                dependencyGraph,
                executionPlan
        );
    }
}
