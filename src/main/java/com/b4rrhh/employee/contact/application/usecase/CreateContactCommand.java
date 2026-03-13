package com.b4rrhh.employee.contact.application.usecase;

public record CreateContactCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String contactTypeCode,
        String contactValue
) {
}
