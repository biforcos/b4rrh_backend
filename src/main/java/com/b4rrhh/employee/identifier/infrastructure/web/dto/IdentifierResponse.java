package com.b4rrhh.employee.identifier.infrastructure.web.dto;

import java.time.LocalDate;

public record IdentifierResponse(
        String identifierTypeCode,
        String identifierValue,
        String issuingCountryCode,
        LocalDate expirationDate,
        boolean isPrimary
) {
}
