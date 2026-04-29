package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import java.util.Map;

public record EmployeePayrollInputErrorResponse(
        String code,
        String message,
        Map<String, Object> details
) {}
