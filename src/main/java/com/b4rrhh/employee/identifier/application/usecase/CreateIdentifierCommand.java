package com.b4rrhh.employee.identifier.application.usecase;

import java.time.LocalDate;

public record CreateIdentifierCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String identifierTypeCode,
        String identifierValue,
        String issuingCountryCode,
        LocalDate expirationDate,
        Boolean isPrimary
) {
}
