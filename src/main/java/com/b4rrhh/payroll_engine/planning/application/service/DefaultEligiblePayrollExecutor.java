package com.b4rrhh.payroll_engine.planning.application.service;

import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.execution.application.service.PocExecutionProjectionHelper;
import com.b4rrhh.payroll_engine.execution.application.service.SegmentExecutionEngine;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.planning.domain.model.EligibleExecutionPlanResult;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationPeriod;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationSegment;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeSegmentBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ExecuteEligiblePayrollUseCase}.
 *
 * <h3>Execution flow</h3>
 * <ol>
 *   <li>Build an {@link EmployeeAssignmentContext} from the request identity fields.</li>
 *   <li>Derive {@code referenceDate} as {@code periodStart}. Eligibility resolution and feed
 *       relation filtering both use that derived date in this first iteration.</li>
 *   <li>Delegate to {@link BuildEligibleExecutionPlanUseCase} to resolve applicable concepts,
 *       expand transitive dependencies, build the dependency graph, and produce the topological
 *       execution plan. This is a single pre-computation step; the plan is reused for every
 *       temporal segment.</li>
 *   <li>Build temporal segments from the working time windows in the request via
 *       {@link WorkingTimeSegmentBuilder}.</li>
 *   <li>For each segment, build a {@link SegmentCalculationContext} and delegate to
 *       {@link SegmentExecutionEngine}. No concept loading or plan rebuild occurs inside
 *       the segment loop: all wiring is pre-resolved by the plan builder.</li>
 *   <li>Extract PoC result concepts from the per-segment
 *       {@link SegmentExecutionState}: {@code SALARIO_BASE}, {@code T_PRECIO_DIA},
 *       {@code PLUS_TRANSPORTE}, {@code TOTAL_DEVENGOS_SEGMENTO},
 *       {@code RETENCION_IRPF_TRAMO}.</li>
 *   <li>Consolidate period-level totals from all segment results.</li>
 * </ol>
 *
 * <h3>PoC limitations on result extraction</h3>
 * <p>The result projection is still PoC-specific (named concept codes). Making it generic
 * is a future concern.

 * <h3>Reference date contract</h3>
 * <p>This use case currently derives {@code referenceDate = request.periodStart} as an intentional
 * first-iteration convention. A future iteration may expose {@code referenceDate} as an explicit
 * input if the use case requires decoupling eligibility date from period start.
 *
 * <h3>Rounding policy</h3>
 * <p>Period-level totals are rounded to scale 2, HALF_UP, after summing segment amounts.
 * Per-segment amounts are already rounded to scale 2, HALF_UP, by
 * {@link com.b4rrhh.payroll_engine.execution.application.service.DefaultSegmentExecutionEngine}.
 *
 * <h3>Empty eligibility result</h3>
 * <p>If the eligibility resolver returns no applicable assignments, the plan is empty,
 * no segment execution is performed, and all period-level totals are zero.
 */
@Service
public class DefaultEligiblePayrollExecutor implements ExecuteEligiblePayrollUseCase {

    private static final int AMOUNT_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BuildEligibleExecutionPlanUseCase planUseCase;
    private final WorkingTimeSegmentBuilder segmentBuilder;
    private final SegmentExecutionEngine segmentExecutionEngine;

    public DefaultEligiblePayrollExecutor(
            BuildEligibleExecutionPlanUseCase planUseCase,
            WorkingTimeSegmentBuilder segmentBuilder,
            SegmentExecutionEngine segmentExecutionEngine
    ) {
        this.planUseCase = planUseCase;
        this.segmentBuilder = segmentBuilder;
        this.segmentExecutionEngine = segmentExecutionEngine;
    }

    @Override
    public EligiblePayrollExecutionResult execute(EligiblePayrollExecutionRequest request) {

        // 1. Build eligibility context from request identity dimensions.
        EmployeeAssignmentContext context = new EmployeeAssignmentContext(
                request.getRuleSystemCode(),
                request.getCompanyCode(),
                request.getAgreementCode(),
                request.getEmployeeTypeCode()
        );

        // 2. First-iteration convention: referenceDate is derived from periodStart.
        LocalDate referenceDate = request.getPeriodStart();

        // 3. Resolve concepts + build execution plan (once, reused for all segments).
        EligibleExecutionPlanResult planResult = planUseCase.build(context, referenceDate);
        List<ConceptExecutionPlanEntry> plan = planResult.executionPlan();

        // 4. Build temporal segments and period context.
        CalculationPeriod period = new CalculationPeriod(request.getPeriodStart(), request.getPeriodEnd());
        long daysInPeriod = ChronoUnit.DAYS.between(period.getPeriodStart(), period.getPeriodEnd()) + 1;
        List<CalculationSegment> segments = segmentBuilder.build(period, request.getWorkingTimeWindows());

        // 5. Execute each segment using the pre-built plan.
        // If the plan is empty (no applicable concepts resolved), skip execution and return zero totals.
        List<SegmentExecutionResult> segmentResults = new ArrayList<>(segments.size());
        if (plan.isEmpty()) {
            return new EligiblePayrollExecutionResult(
                    context, referenceDate, planResult,
                    segmentResults,
                    BigDecimal.ZERO.setScale(AMOUNT_SCALE, ROUNDING),
                    BigDecimal.ZERO.setScale(AMOUNT_SCALE, ROUNDING),
                    BigDecimal.ZERO.setScale(AMOUNT_SCALE, ROUNDING),
                    BigDecimal.ZERO.setScale(AMOUNT_SCALE, ROUNDING)
            );
        }

        for (CalculationSegment segment : segments) {
                        BigDecimal workingTimePercentage = PocExecutionProjectionHelper.resolveWorkingTimePercentage(
                    segment.getSegmentStart(), request.getWorkingTimeWindows());

            SegmentCalculationContext segmentContext = new SegmentCalculationContext(
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
                    request.getMonthlySalaryAmount(),
                    Map.of()
            );

            SegmentExecutionState state = segmentExecutionEngine.execute(plan, segmentContext);

            segmentResults.add(PocExecutionProjectionHelper.toSegmentExecutionResult(
                    request.getRuleSystemCode(),
                    segment,
                    daysInPeriod,
                    workingTimePercentage,
                    state
            ));
        }

        // 6. Consolidate period-level totals.
        PocExecutionProjectionHelper.PocTotals totals =
                PocExecutionProjectionHelper.consolidateTotals(segmentResults, AMOUNT_SCALE, ROUNDING);

        return new EligiblePayrollExecutionResult(
                context,
                referenceDate,
                planResult,
                segmentResults,
                                totals.totalSalarioBase(),
                                totals.totalPlusTransporte(),
                                totals.totalDevengosConsolidated(),
                                totals.totalRetencionIrpf()
        );
    }
}
