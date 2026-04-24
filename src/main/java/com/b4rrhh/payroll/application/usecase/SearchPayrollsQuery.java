package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.PayrollStatus;

public record SearchPayrollsQuery(
        String ruleSystemCode,
        String payrollPeriodCode,
        String employeeNumber,
        PayrollStatus status
) {}
