package com.b4rrhh.employee.infrastructure.web.dto;

public record CreateEmployeeRequest(
        String ruleSystemCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName
) {
}