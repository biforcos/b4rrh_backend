package com.b4rrhh.employee.cost_center.application.usecase;

public record ListCostCenterDistributionHistoryQuery(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
