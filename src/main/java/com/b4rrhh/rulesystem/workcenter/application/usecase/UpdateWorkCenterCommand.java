package com.b4rrhh.rulesystem.workcenter.application.usecase;

public record UpdateWorkCenterCommand(
        String ruleSystemCode,
        String workCenterCode,
        String name,
        String description,
        String companyCode,
        String street,
        String city,
        String postalCode,
        String regionCode,
        String countryCode
) {
}