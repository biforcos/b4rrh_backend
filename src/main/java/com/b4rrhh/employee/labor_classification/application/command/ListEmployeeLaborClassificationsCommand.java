package com.b4rrhh.employee.labor_classification.application.command;

public record ListEmployeeLaborClassificationsCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
