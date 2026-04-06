package com.b4rrhh.rulesystem.company.domain.model;

import java.time.LocalDate;

public record Company(
        String ruleSystemCode,
        String companyCode,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String legalName,
        String taxIdentifier,
        String street,
        String city,
        String postalCode,
        String regionCode,
        String countryCode
) {
}
