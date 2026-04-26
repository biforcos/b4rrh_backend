package com.b4rrhh.payroll.application.port;

public record EmployeePersonalDataContext(
        String fullName,
        String nif,
        String street,
        String city,
        String postalCode
) {}
