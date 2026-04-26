package com.b4rrhh.payroll.infrastructure.web.dto;

public record PayrollEmployeeProfileResponse(
        String fullName,
        String nif,
        String street,
        String city,
        String postalCode
) {}
