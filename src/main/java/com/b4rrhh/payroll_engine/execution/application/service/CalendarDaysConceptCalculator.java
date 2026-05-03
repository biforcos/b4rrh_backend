package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * {@code ENGINE_PROVIDED} calculator for concept D03 — días naturales reales del mes.
 *
 * <p>Returns the actual number of calendar days in the payroll period month:
 * 28 or 29 for February, 30 for April/June/September/November, 31 for the rest.
 * Derived from the period start date, which is always the first day of the month.
 */
@Component
public class CalendarDaysConceptCalculator implements TechnicalConceptCalculator {

    @Override
    public String conceptCode() {
        return "D03";
    }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) {
        return BigDecimal.valueOf(context.periodStart().lengthOfMonth());
    }
}
