package com.b4rrhh.employee.payroll_input.application.usecase;

public record ListEmployeePayrollInputsCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        int period
) {}
