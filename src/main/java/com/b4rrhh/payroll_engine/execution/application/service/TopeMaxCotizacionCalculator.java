package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.SsCotizacionTope;
import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import com.b4rrhh.payroll_engine.execution.domain.port.SsCotizacionTopesRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

@Component
public class TopeMaxCotizacionCalculator implements TechnicalConceptCalculator {

    private final SsCotizacionTopesRepository topesRepository;

    public TopeMaxCotizacionCalculator(SsCotizacionTopesRepository topesRepository) {
        this.topesRepository = topesRepository;
    }

    @Override
    public String conceptCode() { return "P_TOPE_MAX"; }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) {
        BigDecimal baseMax = topesRepository.findActive(
                        context.ruleSystemCode(),
                        context.grupoCotizacionCode(),
                        context.tipoNomina(),
                        context.periodEnd())
                .map(SsCotizacionTope::baseMax)
                .orElseThrow(() -> new IllegalStateException(
                        "No ss_cotizacion_topes entry found for grupo=" + context.grupoCotizacionCode()
                        + " tipoNomina=" + context.tipoNomina()
                        + " referenceDate=" + context.periodEnd()));
        return prorate(baseMax, context);
    }

    private BigDecimal prorate(BigDecimal base, TechnicalConceptSegmentData ctx) {
        if ("DIARIO".equals(ctx.tipoNomina())) {
            return base.multiply(BigDecimal.valueOf(ctx.daysInSegment())).setScale(2, RoundingMode.HALF_UP);
        }
        long daysInPeriod = ChronoUnit.DAYS.between(ctx.periodStart(), ctx.periodEnd()) + 1;
        return base.multiply(BigDecimal.valueOf(ctx.daysInSegment()))
                .divide(BigDecimal.valueOf(daysInPeriod), 2, RoundingMode.HALF_UP);
    }
}
