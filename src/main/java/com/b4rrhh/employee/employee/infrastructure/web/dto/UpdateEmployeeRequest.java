package com.b4rrhh.employee.employee.infrastructure.web.dto;

public record UpdateEmployeeRequest(
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName
) {
}