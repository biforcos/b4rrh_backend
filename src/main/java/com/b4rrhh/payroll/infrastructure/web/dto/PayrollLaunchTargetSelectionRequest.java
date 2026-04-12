package com.b4rrhh.payroll.infrastructure.web.dto;

import com.b4rrhh.payroll.application.usecase.PayrollLaunchTargetSelectionType;

import java.util.List;

public record PayrollLaunchTargetSelectionRequest(
        PayrollLaunchTargetSelectionType selectionType,
        PayrollLaunchEmployeeTargetRequest employee,
        List<PayrollLaunchEmployeeTargetRequest> employees
) {
}