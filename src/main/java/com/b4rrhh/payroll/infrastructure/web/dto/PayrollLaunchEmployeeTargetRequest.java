package com.b4rrhh.payroll.infrastructure.web.dto;

public record PayrollLaunchEmployeeTargetRequest(
        String employeeTypeCode,
        String employeeNumber
) {
}