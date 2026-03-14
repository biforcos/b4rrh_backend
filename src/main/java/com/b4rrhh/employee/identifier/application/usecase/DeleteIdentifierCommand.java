package com.b4rrhh.employee.identifier.application.usecase;

public record DeleteIdentifierCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String identifierTypeCode
) {
}
