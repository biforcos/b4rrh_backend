package com.b4rrhh.payroll.application.usecase;

public record PayrollCalculationUnit(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber
) {
}