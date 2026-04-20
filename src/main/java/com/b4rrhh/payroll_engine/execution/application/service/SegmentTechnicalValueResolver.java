package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.exception.UnsupportedTechnicalConceptException;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Resolves the amount of a technical DIRECT_AMOUNT concept from a {@link SegmentCalculationContext}.
 *
 * <p>This component is PoC-scoped. It is NOT a universal operand resolver.
 * It covers only the minimal set of technical concepts needed for SALARIO_BASE calculation:
 *
 * <ul>
 *   <li>{@code T_DIAS_PRESENCIA_SEGMENTO} = inclusive days in the segment</li>
 *   <li>{@code T_SALARIO_MENSUAL} = monthly salary amount from context</li>
 *   <li>{@code T_FACTOR_JORNADA} = workingTimePercentage / 100</li>
 *   <li>{@code T_PRECIO_DIA} = monthlySalaryAmount / daysInPeriod * (workingTimePercentage / 100)</li>
 * </ul>
 *
 * <h3>Rounding policy</h3>
 * <ul>
 *   <li>Intermediate divisions use scale 8, rounding HALF_UP.</li>
 *   <li>T_PRECIO_DIA result kept at intermediate scale; final rounding occurs at SALARIO_BASE level.</li>
 * </ul>
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

            default -> throw new UnsupportedTechnicalConceptException(conceptCode);
        };
    }
}
