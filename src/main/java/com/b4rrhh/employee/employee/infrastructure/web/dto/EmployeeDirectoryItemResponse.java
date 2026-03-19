package com.b4rrhh.employee.employee.infrastructure.web.dto;

public record EmployeeDirectoryItemResponse(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String displayName,
        String status,
        String workCenterCode
) {
}