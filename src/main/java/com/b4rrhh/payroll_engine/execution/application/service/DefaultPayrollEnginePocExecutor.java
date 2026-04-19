package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import com.b4rrhh.payroll_engine.dependency.application.service.ConceptDependencyGraphService;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingPocConceptException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocRequest;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationPeriod;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationSegment;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeSegmentBuilder;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link PayrollEnginePocExecutor}.
 *
 * <h3>Execution flow</h3>
 * <ol>
 *   <li>Load the three PoC concept definitions from {@link PayrollConceptRepository} by
 *       business key ({@code ruleSystemCode} + {@code conceptCode}).</li>
 *   <li>Derive the {@link ConceptDependencyGraph} from persisted feed relations via
 *       {@link ConceptDependencyGraphService}.</li>
 *   <li>Translate graph + concepts to an ordered execution plan via
 *       {@link ExecutionPlanBuilder}. Plan order is topological: dependencies first.</li>
 *   <li>Build temporal segments from working time windows.</li>
 *   <li>For each segment, build a {@link SegmentCalculationContext} and delegate
 *       execution to {@link SegmentExecutionEngine}.</li>
 *   <li>Extract SALARIO_BASE and T_PRECIO_DIA amounts from the resulting state.</li>
 *   <li>Consolidate {@code totalDevengos} as the sum of all segment salary-base amounts.</li>
 * </ol>
 *
 * <h3>Concept loading and graph derivation</h3>
 * <p>The three required PoC concepts ({@code T_DIAS_PRESENCIA_SEGMENTO}, {@code T_PRECIO_DIA},
 * {@code SALARIO_BASE}) are loaded from {@link PayrollConceptRepository} by business key
 * ({@code ruleSystemCode}, {@code conceptCode}). If any concept is absent,
 * {@link MissingPocConceptException} is thrown immediately.
 *
 * <p>The dependency graph is derived from persisted feed relations via
 * {@link ConceptDependencyGraphService}. Only relations active on the period start date
 * and whose source concept is in the loaded concept set are included.
 *
 * <h3>PoC runtime coupling — RESOLVED</h3>
 * <p>Operand wiring is now configurable from persisted data via
 * {@link com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository}.
 * SALARIO_BASE operand sources are no longer hardcoded in the execution layer.
 * Hardcoded concept code references remain only in {@link SegmentTechnicalValueResolver}
 * (for DIRECT_AMOUNT technical concepts such as T_DIAS_PRESENCIA_SEGMENTO, T_PRECIO_DIA)
 * and in the result extraction at the end of this executor (reading SALARIO_BASE and
 * T_PRECIO_DIA by concept code). These are PoC limitations that should be addressed in a
 * future iteration.
 *
 * <h3>Execution coherence contract</h3>
 * <p>Both graph structure and operand configuration are required to be coherent at runtime:
 * <ul>
 *   <li>The <strong>graph</strong> controls calculation order (topological sort from
 *       persisted feed relations).</li>
 *   <li>The <strong>operand configuration</strong> ({@code payroll_concept_operand} table)
 *       controls which prior-computed value supplies QUANTITY and which supplies RATE for
 *       each RATE_BY_QUANTITY concept.</li>
 *   <li>These two structures must remain aligned: every operand source concept must be a
 *       declared graph dependency of its target. Misalignment is caught at runtime by
 *       {@link RateByQuantityConfigurationValidator} and causes
 *       {@link com.b4rrhh.payroll_engine.execution.domain.exception.OperandGraphMismatchException}.</li>
 * </ul>
 *
 * <h3>Future optimization note</h3>
 * <p>Currently, operand definitions are loaded from the repository once per RATE_BY_QUANTITY
 * concept per segment (inside {@link RateByQuantityOperandResolver}). Both graph and operand
 * structures should be preloaded together at plan-construction time in a future iteration,
 * making per-segment execution fully in-memory.</p>
 *
 * <h3>Rounding policy</h3>
 * <ul>
 *   <li>Intermediate: scale 8, HALF_UP (inside SegmentTechnicalValueResolver).</li>
 *   <li>Per-segment amount: scale 2, HALF_UP (inside DefaultSegmentExecutionEngine).</li>
 *   <li>{@code totalDevengos}: scale 2, HALF_UP applied after summing.</li>
 * </ul>
 */
@Service
public class DefaultPayrollEnginePocExecutor implements PayrollEnginePocExecutor {

    private static final int AMOUNT_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final WorkingTimeSegmentBuilder segmentBuilder;
    private final PayrollConceptRepository conceptRepository;
    private final ConceptDependencyGraphService graphService;
    private final ExecutionPlanBuilder executionPlanBuilder;
    private final SegmentExecutionEngine segmentExecutionEngine;

    public DefaultPayrollEnginePocExecutor(
            WorkingTimeSegmentBuilder segmentBuilder,
            PayrollConceptRepository conceptRepository,
            ConceptDependencyGraphService graphService,
            ExecutionPlanBuilder executionPlanBuilder,
            SegmentExecutionEngine segmentExecutionEngine
    ) {
        this.segmentBuilder = segmentBuilder;
        this.conceptRepository = conceptRepository;
        this.graphService = graphService;
        this.executionPlanBuilder = executionPlanBuilder;
        this.segmentExecutionEngine = segmentExecutionEngine;
    }

    @Override
    public PayrollEnginePocResult execute(PayrollEnginePocRequest request) {
        CalculationPeriod period = new CalculationPeriod(request.getPeriodStart(), request.getPeriodEnd());
        long daysInPeriod = ChronoUnit.DAYS.between(period.getPeriodStart(), period.getPeriodEnd()) + 1;

        // Load the PoC concept definitions from the repository.
        String ruleSystemCode = request.getRuleSystemCode();
        PayrollConcept diasPresencia = loadPocConcept(ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollConcept precioDia    = loadPocConcept(ruleSystemCode, "T_PRECIO_DIA");
        PayrollConcept salarioBase  = loadPocConcept(ruleSystemCode, "SALARIO_BASE");
        List<PayrollConcept> pocConcepts = List.of(diasPresencia, precioDia, salarioBase);

        // Build the dependency graph from persisted feed relations.
        ConceptDependencyGraph graph = graphService.build(pocConcepts, period.getPeriodStart());

        // Translate graph + concept definitions into an ordered execution plan.
        List<ConceptExecutionPlanEntry> plan = executionPlanBuilder.build(graph, pocConcepts);

        List<CalculationSegment> segments = segmentBuilder.build(period, request.getWorkingTimeWindows());

        List<SegmentExecutionResult> segmentResults = new ArrayList<>(segments.size());

        for (CalculationSegment segment : segments) {
            BigDecimal workingTimePercentage = resolveWorkingTimePercentage(
                    segment.getSegmentStart(), request.getWorkingTimeWindows());

            SegmentCalculationContext context = new SegmentCalculationContext(
                    request.getRuleSystemCode(),
                    request.getEmployeeTypeCode(),
                    request.getEmployeeNumber(),
                    period.getPeriodStart(),
                    period.getPeriodEnd(),
                    segment.getSegmentStart(),
                    segment.getSegmentEnd(),
                    segment.isFirstSegment(),
                    segment.isLastSegment(),
                    daysInPeriod,
                    segment.lengthInDaysInclusive(),
                    workingTimePercentage,
                    request.getMonthlySalaryAmount()
            );

            SegmentExecutionState state = segmentExecutionEngine.execute(plan, context, graph);

            BigDecimal salarioBaseAmount = state.getRequiredAmount(
                    new ConceptNodeIdentity(request.getRuleSystemCode(), "SALARIO_BASE"));
            BigDecimal dailyRate = state.getRequiredAmount(
                    new ConceptNodeIdentity(request.getRuleSystemCode(), "T_PRECIO_DIA"));

            segmentResults.add(new SegmentExecutionResult(
                    segment.getSegmentStart(),
                    segment.getSegmentEnd(),
                    segment.isFirstSegment(),
                    segment.isLastSegment(),
                    daysInPeriod,
                    segment.lengthInDaysInclusive(),
                    workingTimePercentage,
                    dailyRate,
                    salarioBaseAmount
            ));
        }

        BigDecimal totalDevengos = segmentResults.stream()
                .map(SegmentExecutionResult::getSalarioBaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(AMOUNT_SCALE, ROUNDING);

        return new PayrollEnginePocResult(segmentResults, totalDevengos);
    }

    /**
     * Loads a single PoC concept from the repository by business key.
     *
     * @throws MissingPocConceptException if the concept is absent from the repository
     */
    private PayrollConcept loadPocConcept(String ruleSystemCode, String conceptCode) {
        return conceptRepository.findByBusinessKey(ruleSystemCode, conceptCode)
                .orElseThrow(() -> new MissingPocConceptException(ruleSystemCode, conceptCode));
    }

    private BigDecimal resolveWorkingTimePercentage(LocalDate date, List<WorkingTimeWindow> windows) {
        for (WorkingTimeWindow window : windows) {
            boolean afterOrOnStart = !date.isBefore(window.getStartDate());
            boolean beforeOrOnEnd  = window.getEndDate() == null || !date.isAfter(window.getEndDate());
            if (afterOrOnStart && beforeOrOnEnd) {
                return window.getWorkingTimePercentage();
            }
        }
        throw new IllegalStateException(
                "No working time window covers date " + date + ". Period coverage should have been validated.");
    }
}
