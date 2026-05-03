package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * {@code ENGINE_PROVIDED} calculator for concept D02 — días de mes nómina.
 *
 * <p>Always returns 30, the conventional payroll month length used as the accrual
 * denominator for monthly concepts. February and 31-day months are treated as 30
 * days for payroll calculation purposes.
 */
@Component
public class PayrollMonthDaysConceptCalculator implements TechnicalConceptCalculator {

    private static final BigDecimal PAYROLL_MONTH_DAYS = new BigDecimal("30");

    @Override
    public String conceptCode() {
        return "D02";
    }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) {
        return PAYROLL_MONTH_DAYS;
    }
}
