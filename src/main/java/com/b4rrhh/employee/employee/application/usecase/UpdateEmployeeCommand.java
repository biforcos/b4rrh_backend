package com.b4rrhh.employee.employee.application.usecase;

public record UpdateEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName
) {
}