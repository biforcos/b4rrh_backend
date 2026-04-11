package com.b4rrhh.payroll.application.port;

public record PayrollEmployeePresenceContext(
        Long employeeId,
        Long presenceId,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer presenceNumber
) {
}