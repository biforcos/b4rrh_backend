package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class WorkingTimeConceptCalculator implements TechnicalConceptCalculator {

    @Override
    public String conceptCode() {
        return "J01";
    }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) {
        return context.workingTimePercentage()
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
    }
}
