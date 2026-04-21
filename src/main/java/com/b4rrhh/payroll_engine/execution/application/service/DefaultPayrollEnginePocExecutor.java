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
 *   <li>Load the eight PoC concept definitions from {@link PayrollConceptRepository} by
 *       business key ({@code ruleSystemCode} + {@code conceptCode}):
 *       {@code T_DIAS_PRESENCIA_SEGMENTO}, {@code T_PRECIO_DIA}, {@code SALARIO_BASE},
 *       {@code T_PRECIO_TRANSPORTE}, {@code PLUS_TRANSPORTE},
 *       {@code TOTAL_DEVENGOS_SEGMENTO}, {@code T_PCT_IRPF}, and
 *       {@code RETENCION_IRPF_TRAMO}.</li>
 *   <li>Derive the {@link ConceptDependencyGraph} from persisted feed relations via
 *       {@link ConceptDependencyGraphService}.</li>
 *   <li>Translate graph + concepts to an ordered, enriched execution plan via
 *       {@link ExecutionPlanBuilder}. Plan order is topological: dependencies first.
 *       Operand wiring ({@code RATE_BY_QUANTITY}) and aggregate sources ({@code AGGREGATE})
 *       are resolved and embedded into each plan entry at this step.</li>
 *   <li>Build temporal segments from working time windows.</li>
 *   <li>For each segment, build a {@link SegmentCalculationContext} and delegate
 *       execution to {@link SegmentExecutionEngine}. No repository access occurs
 *       during per-segment execution: all wiring is pre-resolved in the plan.</li>
 *   <li>Extract per-concept amounts from the resulting state:
 *       {@code SALARIO_BASE}, {@code T_PRECIO_DIA}, {@code PLUS_TRANSPORTE},
 *       {@code TOTAL_DEVENGOS_SEGMENTO}, {@code RETENCION_IRPF_TRAMO}.
 *       These extractions are PoC-specific and not yet generic.</li>
 *   <li>Consolidate period-level totals from the per-segment results.</li>
 * </ol>
 *
 * <h3>Concept loading and graph derivation</h3>
 * <p>The eight PoC concepts are loaded from {@link PayrollConceptRepository} by business key
 * ({@code ruleSystemCode}, {@code conceptCode}). If any concept is absent,
 * {@link MissingPocConceptException} is thrown immediately.
 *
 * <p>The dependency graph is derived from persisted feed relations via
 * {@link ConceptDependencyGraphService}. Only relations active on the period start date
 * and whose source concept is in the loaded concept set are included.
 *
 * <h3>Plan enrichment and in-memory execution</h3>
 * <p>All operand wiring ({@code RATE_BY_QUANTITY}, {@code PERCENTAGE}) and aggregate source lists
 * ({@code AGGREGATE}) are resolved from repositories and the dependency graph at
 * plan-construction time by {@link ExecutionPlanBuilder}. Per-segment execution
 * reads only pre-populated in-memory state: no repository access occurs inside
 * {@link SegmentExecutionEngine#execute}.
 *
 * <h3>Execution coherence contract</h3>
 * <p>Graph structure and operand configuration must remain aligned at plan-construction time:
 * <ul>
 *   <li>The <strong>graph</strong> controls calculation order (topological sort from
 *       persisted feed relations).</li>
 *   <li>The <strong>operand configuration</strong> ({@code payroll_concept_operand} table)
 *       controls which prior-computed value supplies QUANTITY and which supplies RATE for
 *       each {@code RATE_BY_QUANTITY} concept.</li>
 *   <li>Every operand source concept must be a declared graph dependency of its target.
 *       Misalignment is caught at plan-build time by
 *       {@link OperandConfigurationValidator} and causes
 *       {@link com.b4rrhh.payroll_engine.execution.domain.exception.OperandGraphMismatchException}.</li>
 *   <li>Every {@code AGGREGATE} concept must have at least one declared graph dependency.
 *       An empty source set is caught at plan-build time by
 *       {@link com.b4rrhh.payroll_engine.execution.application.service.DefaultExecutionPlanBuilder}
 *       and causes
 *       {@link com.b4rrhh.payroll_engine.execution.domain.exception.MissingAggregateSourcesException}.</li>
 * </ul>
 *
 * <h3>PoC limitations</h3>
 * <p>Hardcoded concept code references remain in {@link SegmentTechnicalValueResolver}
 * (for {@code DIRECT_AMOUNT} technical concepts such as {@code T_DIAS_PRESENCIA_SEGMENTO}
 * and {@code T_PRECIO_DIA}) and in the result extraction at the end of this executor
 * (reading {@code SALARIO_BASE}, {@code T_PRECIO_DIA}, {@code PLUS_TRANSPORTE},
 * {@code TOTAL_DEVENGOS_SEGMENTO} by concept code).
 * Making result extraction generic is a future concern, not part of this PoC.
 *
 * <h3>Rounding policy</h3>
 * <ul>
 *   <li>Intermediate: scale 8, HALF_UP (inside {@link SegmentTechnicalValueResolver}).</li>
 *   <li>Per-segment amounts: scale 2, HALF_UP (inside {@link DefaultSegmentExecutionEngine}).</li>
 *   <li>Period-level totals: scale 2, HALF_UP applied after summing segment amounts.</li>
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
        PayrollConcept diasPresencia         = loadPocConcept(ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollConcept precioDia             = loadPocConcept(ruleSystemCode, "T_PRECIO_DIA");
        PayrollConcept salarioBase           = loadPocConcept(ruleSystemCode, "SALARIO_BASE");
        PayrollConcept precioTransporte      = loadPocConcept(ruleSystemCode, "T_PRECIO_TRANSPORTE");
        PayrollConcept plusTransporte        = loadPocConcept(ruleSystemCode, "PLUS_TRANSPORTE");
        PayrollConcept totalDevengosSegmento = loadPocConcept(ruleSystemCode, "TOTAL_DEVENGOS_SEGMENTO");
        PayrollConcept tPctIrpf              = loadPocConcept(ruleSystemCode, "T_PCT_IRPF");
        PayrollConcept retencionIrpfTramo    = loadPocConcept(ruleSystemCode, "RETENCION_IRPF_TRAMO");
        List<PayrollConcept> pocConcepts = List.of(
                diasPresencia, precioDia, salarioBase, precioTransporte, plusTransporte,
                totalDevengosSegmento, tPctIrpf, retencionIrpfTramo);

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

            SegmentExecutionState state = segmentExecutionEngine.execute(plan, context);

            BigDecimal salarioBaseAmount = state.getRequiredAmount(
                    new ConceptNodeIdentity(request.getRuleSystemCode(), "SALARIO_BASE"));
            BigDecimal dailyRate = state.getRequiredAmount(
                    new ConceptNodeIdentity(request.getRuleSystemCode(), "T_PRECIO_DIA"));
            BigDecimal plusTransporteAmount = state.getRequiredAmount(
                    new ConceptNodeIdentity(request.getRuleSystemCode(), "PLUS_TRANSPORTE"));
            BigDecimal totalDevengosSegmentoAmount = state.getRequiredAmount(
                    new ConceptNodeIdentity(request.getRuleSystemCode(), "TOTAL_DEVENGOS_SEGMENTO"));
            BigDecimal retencionIrpfTramoAmount = state.getRequiredAmount(
                    new ConceptNodeIdentity(request.getRuleSystemCode(), "RETENCION_IRPF_TRAMO"));

            segmentResults.add(new SegmentExecutionResult(
                    segment.getSegmentStart(),
                    segment.getSegmentEnd(),
                    segment.isFirstSegment(),
                    segment.isLastSegment(),
                    daysInPeriod,
                    segment.lengthInDaysInclusive(),
                    workingTimePercentage,
                    dailyRate,
                    salarioBaseAmount,
                    plusTransporteAmount,
                    totalDevengosSegmentoAmount,
                    retencionIrpfTramoAmount
            ));
        }

        BigDecimal totalSalarioBase = segmentResults.stream()
                .map(SegmentExecutionResult::getSalarioBaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(AMOUNT_SCALE, ROUNDING);

        BigDecimal totalPlusTransporte = segmentResults.stream()
                .map(SegmentExecutionResult::getPlusTransporteAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(AMOUNT_SCALE, ROUNDING);

        BigDecimal totalDevengosConsolidated = segmentResults.stream()
                .map(SegmentExecutionResult::getTotalDevengosSegmentoAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(AMOUNT_SCALE, ROUNDING);

        BigDecimal totalRetencionIrpf = segmentResults.stream()
                .map(SegmentExecutionResult::getRetencionIrpfTramoAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(AMOUNT_SCALE, ROUNDING);

        return new PayrollEnginePocResult(segmentResults, totalSalarioBase, totalPlusTransporte,
                totalDevengosConsolidated, totalRetencionIrpf);
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
