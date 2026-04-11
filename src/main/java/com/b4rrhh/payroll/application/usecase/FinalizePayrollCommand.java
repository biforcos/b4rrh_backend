package com.b4rrhh.payroll.application.usecase;

public record FinalizePayrollCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber
) {
}