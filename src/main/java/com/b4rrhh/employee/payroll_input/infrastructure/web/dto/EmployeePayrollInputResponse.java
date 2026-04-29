package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import java.math.BigDecimal;

public record EmployeePayrollInputResponse(
        String conceptCode,
        int period,
        BigDecimal quantity
) {}
