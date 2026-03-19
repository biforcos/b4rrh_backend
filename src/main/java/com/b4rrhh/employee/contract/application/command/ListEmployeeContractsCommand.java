package com.b4rrhh.employee.contract.application.command;

public record ListEmployeeContractsCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
