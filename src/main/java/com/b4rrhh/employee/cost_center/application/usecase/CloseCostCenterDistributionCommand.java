package com.b4rrhh.employee.cost_center.application.usecase;

import java.time.LocalDate;

public record CloseCostCenterDistributionCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate windowStartDate,
        LocalDate endDate
) {
}
