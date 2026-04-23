package com.b4rrhh.employee.working_time.application.service;

import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standard derivation policy for working time hours.
 * Derives weekly, daily, and monthly hours from agreement annual hours and a working time percentage.
 *
 * Calculation assumptions:
 * - 52 weeks per year
 * - 5 working days per week
 * - 12 months per year
 */
@Component
public class StandardWorkingTimeDerivationPolicy implements WorkingTimeDerivationPolicy {

    private static final BigDecimal WEEKS_PER_YEAR = new BigDecimal("52");
    private static final BigDecimal DAYS_PER_WEEK = new BigDecimal("5");
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");
    private static final BigDecimal PERCENTAGE_BASE = new BigDecimal("100");
    private static final int INTERMEDIATE_SCALE = 8;
    private static final int OUTPUT_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public WorkingTimeDerivedHours derive(BigDecimal workingTimePercentage, BigDecimal annualHours) {
        if (workingTimePercentage == null) {
            throw new IllegalArgumentException("workingTimePercentage is required");
        }
        if (annualHours == null) {
            throw new IllegalArgumentException("annualHours is required");
        }

        BigDecimal weeklyFullTimeHours = annualHours.divide(WEEKS_PER_YEAR, INTERMEDIATE_SCALE, ROUNDING_MODE);
        BigDecimal dailyFullTimeHours = weeklyFullTimeHours.divide(DAYS_PER_WEEK, INTERMEDIATE_SCALE, ROUNDING_MODE);
        BigDecimal monthlyFullTimeHours = annualHours.divide(MONTHS_PER_YEAR, INTERMEDIATE_SCALE, ROUNDING_MODE);

        return new WorkingTimeDerivedHours(
                deriveHours(weeklyFullTimeHours, workingTimePercentage),
                deriveHours(dailyFullTimeHours, workingTimePercentage),
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
