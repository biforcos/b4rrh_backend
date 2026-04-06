package com.b4rrhh.rulesystem.company.infrastructure.web.dto;

public record CompanyAddressRequest(
        String street,
        String city,
        String postalCode,
        String regionCode,
        String countryCode
) {
}
