package com.b4rrhh.employee.infrastructure.web;

import com.b4rrhh.employee.application.usecase.CreateEmployeeCommand;
import com.b4rrhh.employee.application.usecase.CreateEmployeeUseCase;
import com.b4rrhh.employee.domain.model.Employee;
import com.b4rrhh.employee.infrastructure.web.dto.CreateEmployeeRequest;
import com.b4rrhh.employee.infrastructure.web.dto.EmployeeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;

    public EmployeeController(CreateEmployeeUseCase createEmployeeUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody CreateEmployeeRequest request) {
        Employee createdEmployee = createEmployeeUseCase.create(
                new CreateEmployeeCommand(
                        request.ruleSystemCode(),
                        request.employeeTypeCode(),
                        request.employeeNumber(),
                        request.firstName(),
                        request.lastName1(),
                        request.lastName2(),
                        request.preferredName()
                )
        );

        return ResponseEntity.status(201).body(toResponse(createdEmployee));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getRuleSystemCode(),
                employee.getEmployeeTypeCode(),
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName1(),
                employee.getLastName2(),
                employee.getPreferredName(),
                employee.getStatus()
        );
    }
}