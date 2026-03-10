package com.b4rrhh.employee.application.usecase;

import com.b4rrhh.employee.domain.model.Employee;

import java.util.Optional;

public interface GetEmployeeByBusinessKeyUseCase {

    Optional<Employee> getByBusinessKey(String ruleSystemCode, String employeeNumber);
}
