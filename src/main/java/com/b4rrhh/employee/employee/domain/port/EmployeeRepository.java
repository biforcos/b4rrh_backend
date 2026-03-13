package com.b4rrhh.employee.employee.domain.port;

import com.b4rrhh.employee.employee.domain.model.Employee;
import java.util.Optional;

public interface EmployeeRepository {

    Optional<Employee> findById(Long id);

    Optional<Employee> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Employee save(Employee employee);
}