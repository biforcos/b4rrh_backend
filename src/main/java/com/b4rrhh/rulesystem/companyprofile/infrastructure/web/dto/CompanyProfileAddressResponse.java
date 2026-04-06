package com.b4rrhh.rulesystem.companyprofile.infrastructure.web.dto;

public record CompanyProfileAddressResponse(
        String street,
        String city,
        String postalCode,
        String regionCode,
        String countryCode
) {
}