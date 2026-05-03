package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SsCcTrabajadorRateCalculator implements TechnicalConceptCalculator {

    private static final BigDecimal RATE = new BigDecimal("4.70");

    @Override
    public String conceptCode() { return "P_SS_CC"; }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) { return RATE; }
}
