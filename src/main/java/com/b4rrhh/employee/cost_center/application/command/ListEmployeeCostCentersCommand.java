package com.b4rrhh.employee.cost_center.application.command;

public record ListEmployeeCostCentersCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
