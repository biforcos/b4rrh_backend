package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SsFogasaEmpresarioRateCalculator implements TechnicalConceptCalculator {

    private static final BigDecimal RATE = new BigDecimal("0.20");

    @Override
    public String conceptCode() { return "P_SS_FOGASA_EMP"; }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) { return RATE; }
}
