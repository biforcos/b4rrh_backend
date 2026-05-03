package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SsDesempleoTrabajadorRateCalculator implements TechnicalConceptCalculator {

    private static final BigDecimal RATE = new BigDecimal("1.55");

    @Override
    public String conceptCode() { return "P_SS_DESEMPLEO"; }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) { return RATE; }
}
