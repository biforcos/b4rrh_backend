package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * {@code ENGINE_PROVIDED} calculator for concept D01 — días de devengo mensual.
 *
 * <p>Returns the number of days in the segment that count toward monthly accrual,
 * capped at 30 per payroll convention. Employees active for the full month in a
 * 31-day month still accrue 30 days; partial-month employees accrue actual days.
 *
 * <p>Formula: {@code min(daysInSegment, 30)}
 */
@Component
public class AccrualDaysConceptCalculator implements TechnicalConceptCalculator {

    private static final long PAYROLL_MONTH_CAP = 30L;

    @Override
    public String conceptCode() {
        return "D01";
    }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) {
        return BigDecimal.valueOf(Math.min(context.daysInSegment(), PAYROLL_MONTH_CAP));
    }
}
