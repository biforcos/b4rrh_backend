package com.b4rrhh.employee.employee.application.usecase;

import com.b4rrhh.employee.employee.domain.model.Employee;

public interface CreateEmployeeUseCase {
    Employee create(CreateEmployeeCommand command);
}