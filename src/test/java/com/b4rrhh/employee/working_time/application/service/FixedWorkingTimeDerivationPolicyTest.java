package com.b4rrhh.employee.working_time.application.service;

import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FixedWorkingTimeDerivationPolicyTest {

    private final StandardWorkingTimeDerivationPolicy policy = new StandardWorkingTimeDerivationPolicy();

    @Test
    void deriveCalculatesHoursFromAnnualHours() {
        BigDecimal percentage = new BigDecimal("100");
        BigDecimal annualHours = new BigDecimal("1560");

        WorkingTimeDerivedHours result = policy.derive(percentage, annualHours);

        assertNotNull(result);
        // 1560 / 52 weeks = 30 hours/week (full time with Spanish standard)
        assertEquals(new BigDecimal("30.00"), result.weeklyHours());
        // 30 / 5 days = 6 hours/day
        assertEquals(new BigDecimal("6.00"), result.dailyHours());
        // 1560 / 12 months = 130 hours/month
        assertEquals(new BigDecimal("130.00"), result.monthlyHours());
    }

    @Test
    void deriveAppliesPercentage() {
        BigDecimal percentage = new BigDecimal("50");
        BigDecimal annualHours = new BigDecimal("1560");

        WorkingTimeDerivedHours result = policy.derive(percentage, annualHours);

        assertNotNull(result);
        // 50% of full time
        assertEquals(new BigDecimal("15.00"), result.weeklyHours());
        assertEquals(new BigDecimal("3.00"), result.dailyHours());
        assertEquals(new BigDecimal("65.00"), result.monthlyHours());
    }

    @Test
    void deriveWorksWithDifferentAnnualHours() {
        BigDecimal percentage = new BigDecimal("100");
        BigDecimal annualHours = new BigDecimal("1800");

        WorkingTimeDerivedHours result = policy.derive(percentage, annualHours);

        assertNotNull(result);
        // 1800 / 52 = 34.62 hours/week
        assertEquals(new BigDecimal("34.62"), result.weeklyHours());
        // 150 / 12 = 150 hours/month
        assertEquals(new BigDecimal("150.00"), result.monthlyHours());
    }

    @Test
    void deriveThrowsWhenPercentageIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                policy.derive(null, new BigDecimal("1560"))
        );
    }

    @Test
    void deriveThrowsWhenAnnualHoursIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                policy.derive(new BigDecimal("100"), null)
        );
    }
}
