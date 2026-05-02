package com.b4rrhh.employee.employee.infrastructure.web.dto;

public record EmployeeResponse(
        Long id,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        String status,
        String photoUrl
) {
}
