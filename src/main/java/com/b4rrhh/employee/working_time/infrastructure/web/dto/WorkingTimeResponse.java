package com.b4rrhh.employee.working_time.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkingTimeResponse(
        Integer workingTimeNumber,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal workingTimePercentage,
        BigDecimal weeklyHours,
        BigDecimal dailyHours,
        BigDecimal monthlyHours
) {
}