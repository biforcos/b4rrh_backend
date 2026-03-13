package com.b4rrhh.employee.employee.application.usecase;

import com.b4rrhh.employee.employee.domain.model.Employee;

import java.util.Optional;

public interface GetEmployeeByBusinessKeyUseCase {

    Optional<Employee> getByBusinessKey(String ruleSystemCode, String employeeTypeCode, String employeeNumber);
}
