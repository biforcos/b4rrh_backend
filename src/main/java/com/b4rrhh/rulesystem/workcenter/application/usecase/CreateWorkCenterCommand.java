package com.b4rrhh.rulesystem.workcenter.application.usecase;

import java.time.LocalDate;

public record CreateWorkCenterCommand(
        String ruleSystemCode,
        String workCenterCode,
        String name,
        String description,
        LocalDate startDate,
        String companyCode,
        String street,
        String city,
        String postalCode,
        String regionCode,
        String countryCode
) {
}