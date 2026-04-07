package com.b4rrhh.employee.working_time.domain.model;

import java.math.BigDecimal;

public record WorkingTimeDerivedHours(
        BigDecimal weeklyHours,
        BigDecimal dailyHours,
        BigDecimal monthlyHours
) {
}