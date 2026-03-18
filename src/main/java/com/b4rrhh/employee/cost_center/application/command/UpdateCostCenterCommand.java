package com.b4rrhh.employee.cost_center.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCostCenterCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String costCenterCode,
        LocalDate startDate,
        BigDecimal allocationPercentage
) {
}
