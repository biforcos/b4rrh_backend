package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.math.BigDecimal;

public record RehireEmployeeWorkingTimeRequest(
        BigDecimal workingTimePercentage,
        BigDecimal weeklyHours,
        BigDecimal dailyHours,
        BigDecimal monthlyHours
) {
}