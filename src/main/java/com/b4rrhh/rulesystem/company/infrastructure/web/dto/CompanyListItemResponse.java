package com.b4rrhh.rulesystem.company.infrastructure.web.dto;

import java.time.LocalDate;

public record CompanyListItemResponse(
        String ruleSystemCode,
        String companyCode,
        String name,
        String legalName,
        String taxIdentifier,
        String countryCode,
        boolean active,
        LocalDate startDate,
        LocalDate endDate
) {
}
