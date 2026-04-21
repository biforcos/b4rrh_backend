package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedTechnicalConceptException;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Resolves the amount of a technical {@code DIRECT_AMOUNT} concept from a
 * {@link SegmentCalculationContext}.
 *
 * <h3>PoC scope — hardcoded values only</h3>
 * <p>This component is PoC-scoped. All values are hardcoded or derived directly from the
 * segment context. It is <strong>not</strong> a rule engine, collective agreement resolver,
 * or tax calculation service. It exists solely to supply the fixed technical inputs required
 * for the PoC execution plan.
 *
 * <h3>Supported concepts</h3>
 * <ul>
 *   <li>{@code T_DIAS_PRESENCIA_SEGMENTO} — inclusive days in the segment</li>
 *   <li>{@code T_SALARIO_MENSUAL} — monthly salary amount from context</li>
 *   <li>{@code T_FACTOR_JORNADA} — workingTimePercentage / 100</li>
 *   <li>{@code T_PRECIO_DIA} — monthlySalaryAmount / daysInPeriod × (workingTimePercentage / 100)</li>
 *   <li>{@code T_PRECIO_TRANSPORTE} — fixed daily transport rate (hardcoded PoC value)</li>
 *   <li>{@code T_PCT_IRPF} — fixed IRPF withholding percentage (hardcoded PoC value)</li>
 * </ul>
 *
 * <h3>Rounding policy</h3>
 * <ul>
 *   <li>Intermediate divisions use scale 8, rounding HALF_UP.</li>
 *   <li>{@code T_PRECIO_DIA} result kept at intermediate scale; final rounding occurs at the
 *       consuming concept level.</li>
 * </ul>
 *
 * <h3>PoC warning</h3>
 * <p>In a real implementation, {@code T_PRECIO_TRANSPORTE} would come from a collective
 * agreement table and {@code T_PCT_IRPF} would come from employee tax data. Neither is
 * computed here.
 */
@Component
public class SegmentTechnicalValueResolver {

    private static final int INTERMEDIATE_SCALE = 8;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Resolves the value of a technical concept from the given segment context.
     *
     * @throws UnsupportedTechnicalConceptException if the concept code is not recognised
     */
    public BigDecimal resolve(String conceptCode, SegmentCalculationContext context) {
        return switch (conceptCode) {
            case "T_DIAS_PRESENCIA_SEGMENTO" ->
                    BigDecimal.valueOf(context.getDaysInSegment());

            case "T_SALARIO_MENSUAL" ->
                    context.getMonthlySalaryAmount();

            case "T_FACTOR_JORNADA" ->
                    context.getWorkingTimePercentage()
                            .divide(BigDecimal.valueOf(100), INTERMEDIATE_SCALE, ROUNDING);

            case "T_PRECIO_DIA" ->
                    context.getMonthlySalaryAmount()
                            .divide(BigDecimal.valueOf(context.getDaysInPeriod()), INTERMEDIATE_SCALE, ROUNDING)
                            .multiply(
                                    context.getWorkingTimePercentage()
                                            .divide(BigDecimal.valueOf(100), INTERMEDIATE_SCALE, ROUNDING)
                            )
                            .setScale(INTERMEDIATE_SCALE, ROUNDING);

            // Fixed daily transport rate, independent of working time percentage.
            // PoC value; in a real implementation this would come from a collective agreement table.
            case "T_PRECIO_TRANSPORTE" ->
                    new BigDecimal("7.50");

            // Fixed IRPF withholding rate for the PoC.
            // This is NOT a tax engine. It is a single flat percentage hardcoded for PoC testing only.
            // In a real implementation this value would come from employee tax data or collective agreement.
            case "T_PCT_IRPF" ->
                    new BigDecimal("15");

            default -> throw new UnsupportedTechnicalConceptException(conceptCode);
        };
    }
}
