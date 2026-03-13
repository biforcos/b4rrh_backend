package com.b4rrhh.employee.address.application.usecase;

import java.time.LocalDate;

public record CreateAddressCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String addressTypeCode,
        String street,
        String city,
        String countryCode,
        String postalCode,
        String regionCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
