package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionResult;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.segment.domain.model.CalculationSegment;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Small PoC-specific helper for projecting segment state and consolidating totals.
 *
 * <p>This helper intentionally keeps PoC concept-code extraction explicit
 * ({@code SALARIO_BASE}, {@code T_PRECIO_DIA}, {@code PLUS_TRANSPORTE},
 * {@code TOTAL_DEVENGOS_SEGMENTO}, {@code RETENCION_IRPF_TRAMO}).
 * It is not a generic result framework.
 */
public final class PocExecutionProjectionHelper {

    private PocExecutionProjectionHelper() {
    }

    public static BigDecimal resolveWorkingTimePercentage(LocalDate date, List<WorkingTimeWindow> windows) {
        for (WorkingTimeWindow window : windows) {
            boolean afterOrOnStart = !date.isBefore(window.getStartDate());
            boolean beforeOrOnEnd = window.getEndDate() == null || !date.isAfter(window.getEndDate());
            if (afterOrOnStart && beforeOrOnEnd) {
                return window.getWorkingTimePercentage();
            }
        }
        throw new IllegalStateException(
                "No working time window covers date " + date + ". Period coverage should have been validated.");
    }

    public static SegmentExecutionResult toSegmentExecutionResult(
            String ruleSystemCode,
            CalculationSegment segment,
            long daysInPeriod,
            BigDecimal workingTimePercentage,
            SegmentExecutionState state
    ) {
        BigDecimal salarioBaseAmount = state.getRequiredAmount(
                new ConceptNodeIdentity(ruleSystemCode, "SALARIO_BASE"));
        BigDecimal dailyRate = state.getRequiredAmount(
                new ConceptNodeIdentity(ruleSystemCode, "T_PRECIO_DIA"));
        BigDecimal plusTransporteAmount = state.getRequiredAmount(
                new ConceptNodeIdentity(ruleSystemCode, "PLUS_TRANSPORTE"));
        BigDecimal totalDevengosSegmentoAmount = state.getRequiredAmount(
                new ConceptNodeIdentity(ruleSystemCode, "TOTAL_DEVENGOS_SEGMENTO"));
        BigDecimal retencionIrpfTramoAmount = state.getRequiredAmount(
                new ConceptNodeIdentity(ruleSystemCode, "RETENCION_IRPF_TRAMO"));

        return new SegmentExecutionResult(
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
        );
    }

    public static PocTotals consolidateTotals(
            List<SegmentExecutionResult> segmentResults,
            int amountScale,
            RoundingMode rounding
    ) {
        BigDecimal totalSalarioBase = segmentResults.stream()
                .map(SegmentExecutionResult::getSalarioBaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(amountScale, rounding);

        BigDecimal totalPlusTransporte = segmentResults.stream()
                .map(SegmentExecutionResult::getPlusTransporteAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(amountScale, rounding);

        BigDecimal totalDevengosConsolidated = segmentResults.stream()
                .map(SegmentExecutionResult::getTotalDevengosSegmentoAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(amountScale, rounding);

        BigDecimal totalRetencionIrpf = segmentResults.stream()
                .map(SegmentExecutionResult::getRetencionIrpfTramoAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(amountScale, rounding);

        return new PocTotals(
                totalSalarioBase,
                totalPlusTransporte,
                totalDevengosConsolidated,
                totalRetencionIrpf
        );
    }

    public record PocTotals(
            BigDecimal totalSalarioBase,
            BigDecimal totalPlusTransporte,
            BigDecimal totalDevengosConsolidated,
            BigDecimal totalRetencionIrpf
    ) {
    }
}
