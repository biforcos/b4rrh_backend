package com.b4rrhh.employee.cost_center.application.usecase;

public record GetCurrentCostCenterDistributionQuery(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
