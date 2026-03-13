package com.b4rrhh.employee.contact.application.usecase;

public record DeleteContactCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String contactTypeCode
) {
}
