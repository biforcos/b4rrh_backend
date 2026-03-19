package com.b4rrhh.employee.employee.infrastructure.persistence;

public record EmployeeDirectoryProjection(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        String status,
        String workCenterCode
) {
}