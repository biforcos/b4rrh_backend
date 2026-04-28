package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;

public interface UpdateEmployeePayrollInputUseCase {
    EmployeePayrollInput update(UpdateEmployeePayrollInputCommand command);
}
