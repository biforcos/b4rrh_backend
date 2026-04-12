package com.b4rrhh.payroll.application.usecase;

public record PayrollLaunchEmployeeTarget(
        String employeeTypeCode,
        String employeeNumber
) {
}