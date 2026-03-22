package com.b4rrhh.employee.address.application.usecase;

public record UpdateAddressCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer addressNumber,
        String street,
        String city,
        String countryCode,
        String postalCode,
        String regionCode
) {
}
