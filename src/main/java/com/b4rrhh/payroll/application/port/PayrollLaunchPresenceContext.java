package com.b4rrhh.payroll.application.port;

public record PayrollLaunchPresenceContext(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer presenceNumber
) {
}