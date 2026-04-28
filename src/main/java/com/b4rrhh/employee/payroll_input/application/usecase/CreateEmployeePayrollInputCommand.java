package com.b4rrhh.employee.payroll_input.application.usecase;

import java.math.BigDecimal;

public record CreateEmployeePayrollInputCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String conceptCode,
        int period,
        BigDecimal quantity
) {}
