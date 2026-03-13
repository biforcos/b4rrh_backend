package com.b4rrhh.employee.contact.application.usecase;

public record UpdateContactCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String contactTypeCode,
        String contactValue
) {
}
