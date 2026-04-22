package com.b4rrhh.payroll.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollLaunchWorkingTimeWindowContext(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal workingTimePercentage
) {
}
