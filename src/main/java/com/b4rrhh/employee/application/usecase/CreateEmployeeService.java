package com.b4rrhh.employee.application.usecase;

import com.b4rrhh.employee.domain.model.Employee;
import com.b4rrhh.employee.domain.port.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class CreateEmployeeService implements CreateEmployeeUseCase {

    private final EmployeeRepository employeeRepository;

    public CreateEmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee create(CreateEmployeeCommand command) {
        employeeRepository.findByRuleSystemCodeAndEmployeeNumber(
                command.ruleSystemCode(),
                command.employeeNumber()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Employee already exists with ruleSystemCode and employeeNumber");
        });

        Employee newEmployee = new Employee(
                null, // id
                command.ruleSystemCode(),
                command.employeeNumber(),
                command.firstName(),
                command.lastName1(),
                command.lastName2(),
                command.preferredName(),
                "ACTIVE", // status
                LocalDateTime.now(), // createdAt
                LocalDateTime.now()  // updatedAt
        );

        return employeeRepository.save(newEmployee);
    }
}