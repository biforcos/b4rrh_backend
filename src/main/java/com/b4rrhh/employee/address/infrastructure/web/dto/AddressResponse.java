package com.b4rrhh.employee.address.infrastructure.web.dto;

import java.time.LocalDate;

public record AddressResponse(
        Integer addressNumber,
        String addressTypeCode,
        String addressTypeName,
        String street,
        String city,
        String countryCode,
        String postalCode,
        String regionCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
