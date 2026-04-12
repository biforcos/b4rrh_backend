package com.b4rrhh.payroll.application.port;

public record PayrollLaunchEmployeeContext(
        String employeeTypeCode,
        String employeeNumber
) {
}