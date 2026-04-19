package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.ExecutionScope;
import com.b4rrhh.payroll_engine.concept.domain.model.FunctionalNature;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.ResultCompositionMode;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraphBuilder;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocRequest;
import com.b4rrhh.payroll_engine.execution.domain.model.PayrollEnginePocResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
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
 *   <li>Build the PoC concept set in-memory (T_DIAS_PRESENCIA_SEGMENTO, T_PRECIO_DIA,
 *       SALARIO_BASE).</li>
 *   <li>Build the corresponding {@link ConceptDependencyGraph} declaring that
 *       SALARIO_BASE depends on both technical concepts.</li>
 *   <li>Translate graph + concepts to an ordered execution plan via
 *       {@link ExecutionPlanBuilder}. Plan order is topological: dependencies first.</li>
 *   <li>Build temporal segments from working time windows.</li>
 *   <li>For each segment, build a {@link SegmentCalculationContext} and delegate
 *       execution to {@link SegmentExecutionEngine}.</li>
 *   <li>Extract SALARIO_BASE and T_PRECIO_DIA amounts from the resulting state.</li>
 *   <li>Consolidate {@code totalDevengos} as the sum of all segment salary-base amounts.</li>
 * </ol>
 *
 * <h3>PoC in-memory concept assembly — TEMPORARY</h3>
 * <p><strong>This assembly is a PoC placeholder only.</strong>
 * The three concepts ({@code T_DIAS_PRESENCIA_SEGMENTO}, {@code T_PRECIO_DIA},
 * {@code SALARIO_BASE}) are constructed directly in memory because repository
 * loading is outside the scope of the current iteration.
 *
 * <p>Once concepts can be loaded from the {@code payroll_engine} repositories,
 * {@link #buildPocConcepts} and {@link #buildPocGraph} must be removed entirely
 * and replaced by repository-sourced data. These helpers must <strong>not</strong>
 * be extended with real business logic or grow into a second hidden source of truth
 * for concept definitions.
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
    private final ExecutionPlanBuilder executionPlanBuilder;
    private final SegmentExecutionEngine segmentExecutionEngine;

    public DefaultPayrollEnginePocExecutor(
            WorkingTimeSegmentBuilder segmentBuilder,
            ExecutionPlanBuilder executionPlanBuilder,
            SegmentExecutionEngine segmentExecutionEngine
    ) {
        this.segmentBuilder = segmentBuilder;
        this.executionPlanBuilder = executionPlanBuilder;
        this.segmentExecutionEngine = segmentExecutionEngine;
    }

    @Override
    public PayrollEnginePocResult execute(PayrollEnginePocRequest request) {
        CalculationPeriod period = new CalculationPeriod(request.getPeriodStart(), request.getPeriodEnd());
        long daysInPeriod = ChronoUnit.DAYS.between(period.getPeriodStart(), period.getPeriodEnd()) + 1;

        // Build the in-memory PoC concept set for this rule system.
        List<PayrollConcept> pocConcepts = buildPocConcepts(request.getRuleSystemCode());

        // Build the structural dependency graph for these concepts.
        ConceptDependencyGraph graph = buildPocGraph(pocConcepts, request.getRuleSystemCode());

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
     * Builds the minimal set of PoC concepts in memory.
     *
     * <p><strong>TEMPORARY — remove when concepts are loaded from repositories.</strong>
     * This method exists only to unblock the graph → execution-plan bridge PoC.
     * It must not be extended with business logic. Any change to concept definitions
     * must go through the proper {@code payroll_engine} concept and object domain,
     * not through this helper.
     *
     * <ul>
     *   <li>{@code T_DIAS_PRESENCIA_SEGMENTO} — DIRECT_AMOUNT, INFORMATIONAL, SEGMENT scope</li>
     *   <li>{@code T_PRECIO_DIA} — DIRECT_AMOUNT, INFORMATIONAL, SEGMENT scope</li>
     *   <li>{@code SALARIO_BASE} — RATE_BY_QUANTITY, EARNING, SEGMENT scope</li>
     * </ul>
     *
     * <p>Fields beyond {@code calculationType} (mnemonic, compositionMode, payslipOrderCode)
     * are set to placeholder values because only {@code calculationType} is used at execution time.
     */
    private List<PayrollConcept> buildPocConcepts(String ruleSystemCode) {
        PayrollConcept diasPresencia = pocConcept(
                ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO",
                CalculationType.DIRECT_AMOUNT, FunctionalNature.INFORMATIONAL);

        PayrollConcept precioDia = pocConcept(
                ruleSystemCode, "T_PRECIO_DIA",
                CalculationType.DIRECT_AMOUNT, FunctionalNature.INFORMATIONAL);

        PayrollConcept salarioBase = pocConcept(
                ruleSystemCode, "SALARIO_BASE",
                CalculationType.RATE_BY_QUANTITY, FunctionalNature.EARNING);

        return List.of(diasPresencia, precioDia, salarioBase);
    }

    private PayrollConcept pocConcept(
            String ruleSystemCode,
            String conceptCode,
            CalculationType calculationType,
            FunctionalNature nature
    ) {
        PayrollObject object = new PayrollObject(
                null, ruleSystemCode, PayrollObjectTypeCode.CONCEPT, conceptCode, null, null);
        return new PayrollConcept(
                object,
                conceptCode,          // mnemonic = conceptCode for PoC
                calculationType,
                nature,
                ResultCompositionMode.REPLACE,
                null,                 // payslipOrderCode — not relevant for PoC execution
                ExecutionScope.SEGMENT,
                null, null
        );
    }

    /**
     * Builds the structural dependency graph for the PoC:
     * SALARIO_BASE depends on T_DIAS_PRESENCIA_SEGMENTO and T_PRECIO_DIA.
     */
    private ConceptDependencyGraph buildPocGraph(List<PayrollConcept> concepts, String ruleSystemCode) {
        PayrollConcept diasPresencia = findConcept(concepts, ruleSystemCode, "T_DIAS_PRESENCIA_SEGMENTO");
        PayrollConcept precioDia    = findConcept(concepts, ruleSystemCode, "T_PRECIO_DIA");
        PayrollConcept salarioBase  = findConcept(concepts, ruleSystemCode, "SALARIO_BASE");

        return new ConceptDependencyGraphBuilder()
                .addNode(diasPresencia)
                .addNode(precioDia)
                .addOperandDependency(salarioBase, diasPresencia)
                .addOperandDependency(salarioBase, precioDia)
                .build();
    }

    private PayrollConcept findConcept(List<PayrollConcept> concepts, String ruleSystemCode, String conceptCode) {
        return concepts.stream()
                .filter(c -> c.getRuleSystemCode().equals(ruleSystemCode)
                          && c.getConceptCode().equals(conceptCode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "PoC concept not found: " + ruleSystemCode + "/" + conceptCode));
    }

    /**
     * Locates the working-time window that covers {@code date}.
     *
     * @throws IllegalStateException if no window covers the date
     */
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
