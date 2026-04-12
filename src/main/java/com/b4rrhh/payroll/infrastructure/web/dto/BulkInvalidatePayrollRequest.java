package com.b4rrhh.payroll.infrastructure.web.dto;

public record BulkInvalidatePayrollRequest(
        String ruleSystemCode,
        String payrollPeriodCode,
        String payrollTypeCode,
        String statusReasonCode,
        PayrollLaunchTargetSelectionRequest targetSelection
) {
}
