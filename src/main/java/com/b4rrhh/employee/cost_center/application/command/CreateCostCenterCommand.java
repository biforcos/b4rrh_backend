package com.b4rrhh.employee.cost_center.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCostCenterCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String costCenterCode,
        BigDecimal allocationPercentage,
        LocalDate startDate,
        LocalDate endDate
) {
}
