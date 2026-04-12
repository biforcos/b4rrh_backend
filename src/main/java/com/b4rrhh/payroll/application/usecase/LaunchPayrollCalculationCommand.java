package com.b4rrhh.payroll.application.usecase;

public record LaunchPayrollCalculationCommand(
        String ruleSystemCode,
        String payrollPeriodCode,
        String payrollTypeCode,
        String calculationEngineCode,
        String calculationEngineVersion,
        PayrollLaunchTargetSelection targetSelection
) {
}