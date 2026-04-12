package com.b4rrhh.payroll.application.usecase;

public record CalculatePayrollUnitCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber,
        String calculationEngineCode,
        String calculationEngineVersion
) {
}