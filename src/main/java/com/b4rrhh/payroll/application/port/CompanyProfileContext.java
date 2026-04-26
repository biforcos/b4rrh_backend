package com.b4rrhh.payroll.application.port;

public record CompanyProfileContext(
        String legalName,
        String taxIdentifier,
        String street,
        String city,
        String postalCode
) {}
