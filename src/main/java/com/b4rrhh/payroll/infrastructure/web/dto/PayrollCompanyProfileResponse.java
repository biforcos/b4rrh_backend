package com.b4rrhh.payroll.infrastructure.web.dto;

public record PayrollCompanyProfileResponse(
        String legalName,
        String taxIdentifier,
        String street,
        String city,
        String postalCode
) {}
