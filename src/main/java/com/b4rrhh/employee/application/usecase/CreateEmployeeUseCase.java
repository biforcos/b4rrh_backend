package com.b4rrhh.employee.application.usecase;

import com.b4rrhh.employee.domain.model.Employee;

public interface CreateEmployeeUseCase {
    Employee create(CreateEmployeeCommand command);
}