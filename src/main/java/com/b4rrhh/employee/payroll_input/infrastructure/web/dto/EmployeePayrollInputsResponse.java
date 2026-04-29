package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import java.util.List;

public record EmployeePayrollInputsResponse(
        int period,
        List<EmployeePayrollInputResponse> inputs
) {}
