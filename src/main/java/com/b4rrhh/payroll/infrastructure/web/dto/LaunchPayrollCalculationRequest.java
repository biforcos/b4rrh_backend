package com.b4rrhh.payroll.infrastructure.web.dto;

public record LaunchPayrollCalculationRequest(
        String ruleSystemCode,
        String payrollPeriodCode,
        String payrollTypeCode,
        String calculationEngineCode,
        String calculationEngineVersion,
        PayrollLaunchTargetSelectionRequest targetSelection
) {
}