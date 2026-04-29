package com.b4rrhh.employee.payroll_input.application.usecase;

public record DeleteEmployeePayrollInputCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String conceptCode,
        int period
) {}
