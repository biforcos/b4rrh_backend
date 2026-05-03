package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SsCcEmpresarioRateCalculator implements TechnicalConceptCalculator {

    private static final BigDecimal RATE = new BigDecimal("23.60");

    @Override
    public String conceptCode() { return "P_SS_CC_EMP"; }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) { return RATE; }
}
