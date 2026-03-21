package com.b4rrhh.employee.employee.application.usecase;

import com.b4rrhh.employee.employee.domain.model.Employee;

public interface UpdateEmployeeUseCase {

    Employee update(UpdateEmployeeCommand command);
}