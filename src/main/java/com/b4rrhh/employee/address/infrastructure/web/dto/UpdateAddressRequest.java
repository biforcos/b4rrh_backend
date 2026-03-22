package com.b4rrhh.employee.address.infrastructure.web.dto;

public record UpdateAddressRequest(
        String street,
        String city,
        String countryCode,
        String postalCode,
        String regionCode
) {
}
