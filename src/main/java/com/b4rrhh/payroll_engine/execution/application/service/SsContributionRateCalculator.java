package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * {@code JAVA_PROVIDED} calculator for concept P_SS — tipo de cotización SS trabajador.
 *
 * <p>Returns the fixed employee Social Security contribution rate (6.35%).
 * This is a flat rate per the Spanish general regime (contingencias comunes 4.70% +
 * desempleo 1.55% + formación 0.10%). The value is fixed here until proper
 * agreement/employee-level configuration is available.
 */
@Component
public class SsContributionRateCalculator implements TechnicalConceptCalculator {

    private static final BigDecimal SS_RATE = new BigDecimal("6.35");

    @Override
    public String conceptCode() {
        return "P_SS";
    }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) {
        return SS_RATE;
    }
}
