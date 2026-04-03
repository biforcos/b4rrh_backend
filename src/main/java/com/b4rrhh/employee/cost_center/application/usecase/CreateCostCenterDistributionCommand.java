package com.b4rrhh.employee.cost_center.application.usecase;

import java.time.LocalDate;
import java.util.List;

public record CreateCostCenterDistributionCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate,
        List<CostCenterDistributionItem> items
) {
}
