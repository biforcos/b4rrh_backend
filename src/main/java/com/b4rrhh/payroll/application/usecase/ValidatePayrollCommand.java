package com.b4rrhh.payroll.application.usecase;

public record ValidatePayrollCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber
) {
}