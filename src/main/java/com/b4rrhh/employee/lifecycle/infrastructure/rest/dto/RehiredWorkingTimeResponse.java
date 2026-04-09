package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RehiredWorkingTimeResponse(
        Integer workingTimeNumber,
        BigDecimal workingTimePercentage,
        BigDecimal weeklyHours,
        BigDecimal dailyHours,
        BigDecimal monthlyHours,
        LocalDate startDate,
        LocalDate endDate
) {
}