package com.b4rrhh.employee.working_time.application.service;

import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedWorkingTimeDerivationPolicy implements WorkingTimeDerivationPolicy {

    private static final BigDecimal FULL_TIME_ANNUAL_HOURS = new BigDecimal("2000");
    private static final BigDecimal FULL_TIME_WEEKLY_HOURS = new BigDecimal("40");
    private static final BigDecimal FULL_TIME_DAILY_HOURS = new BigDecimal("8");
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");
    private static final BigDecimal PERCENTAGE_BASE = new BigDecimal("100");
    private static final int INTERMEDIATE_SCALE = 8;
    private static final int OUTPUT_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public WorkingTimeDerivedHours derive(BigDecimal workingTimePercentage) {
        if (workingTimePercentage == null) {
            throw new IllegalArgumentException("workingTimePercentage is required");
        }

        BigDecimal monthlyFullTimeHours = FULL_TIME_ANNUAL_HOURS.divide(
                MONTHS_PER_YEAR,
                INTERMEDIATE_SCALE,
                ROUNDING_MODE
        );

        return new WorkingTimeDerivedHours(
                deriveHours(FULL_TIME_WEEKLY_HOURS, workingTimePercentage),
                deriveHours(FULL_TIME_DAILY_HOURS, workingTimePercentage),
                deriveHours(monthlyFullTimeHours, workingTimePercentage)
        );
    }

    private BigDecimal deriveHours(BigDecimal fullTimeHours, BigDecimal workingTimePercentage) {
        return fullTimeHours
                .multiply(workingTimePercentage)
                .divide(PERCENTAGE_BASE, INTERMEDIATE_SCALE, ROUNDING_MODE)
                .setScale(OUTPUT_SCALE, ROUNDING_MODE);
    }
}