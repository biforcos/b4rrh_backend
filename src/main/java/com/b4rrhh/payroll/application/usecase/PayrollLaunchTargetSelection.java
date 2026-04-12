package com.b4rrhh.payroll.application.usecase;

import java.util.List;

public record PayrollLaunchTargetSelection(
        PayrollLaunchTargetSelectionType selectionType,
        PayrollLaunchEmployeeTarget employee,
        List<PayrollLaunchEmployeeTarget> employees
) {
}