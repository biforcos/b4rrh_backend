package com.b4rrhh.employee.application.usecase;

public record CreateEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName
) {
}