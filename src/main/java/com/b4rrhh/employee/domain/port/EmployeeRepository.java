package com.b4rrhh.employee.domain.port;

import com.b4rrhh.employee.domain.model.Employee;
import java.util.Optional;

public interface EmployeeRepository {

    Optional<Employee> findByRuleSystemCodeAndEmployeeNumber(String ruleSystemCode, String employeeNumber);

    Employee save(Employee employee);
}