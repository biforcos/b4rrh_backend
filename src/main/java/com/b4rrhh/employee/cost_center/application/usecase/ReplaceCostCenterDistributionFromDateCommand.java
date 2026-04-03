package com.b4rrhh.employee.cost_center.application.usecase;

import java.time.LocalDate;
import java.util.List;

public record ReplaceCostCenterDistributionFromDateCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate effectiveDate,
        List<CostCenterDistributionItem> items
) {
}
