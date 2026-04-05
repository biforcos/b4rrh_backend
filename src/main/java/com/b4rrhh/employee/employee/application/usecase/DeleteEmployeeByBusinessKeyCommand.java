package com.b4rrhh.employee.employee.application.usecase;

public record DeleteEmployeeByBusinessKeyCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
