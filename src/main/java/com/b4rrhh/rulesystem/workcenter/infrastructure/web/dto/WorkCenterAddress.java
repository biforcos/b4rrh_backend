package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

public record WorkCenterAddress(
        String street,
        String city,
        String postalCode,
        String regionCode,
        String countryCode
) {
}