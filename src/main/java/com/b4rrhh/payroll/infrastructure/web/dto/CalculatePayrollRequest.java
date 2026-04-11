package com.b4rrhh.payroll.infrastructure.web.dto;

import com.b4rrhh.payroll.domain.model.PayrollStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CalculatePayrollRequest(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber,
        PayrollStatus status,
        String statusReasonCode,
        LocalDateTime calculatedAt,
        String calculationEngineCode,
        String calculationEngineVersion,
        List<PayrollConceptRequest> concepts,
        List<PayrollContextSnapshotRequest> contextSnapshots
) {
}