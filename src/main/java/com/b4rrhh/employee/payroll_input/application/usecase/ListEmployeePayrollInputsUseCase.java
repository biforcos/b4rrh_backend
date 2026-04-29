package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;

import java.util.List;

public interface ListEmployeePayrollInputsUseCase {
    List<EmployeePayrollInput> listByEmployeeAndPeriod(ListEmployeePayrollInputsCommand command);
}
