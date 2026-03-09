package com.b4rrhh.employee.infrastructure.web;

import com.b4rrhh.employee.application.usecase.CreateEmployeeUseCase;
import com.b4rrhh.employee.application.usecase.CreateEmployeeCommand;
import com.b4rrhh.employee.domain.model.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final CreateEmployeeUseCase createEmployeeUseCase;

    public EmployeeController(CreateEmployeeUseCase createEmployeeUseCase) {
        this.createEmployeeUseCase = createEmployeeUseCase;
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequest request) {
        Employee createdEmployee = createEmployeeUseCase.create(
                new CreateEmployeeCommand(
                        request.getRuleSystemCode(),
                        request.getEmployeeNumber(),
                        request.getFirstName(),
                        request.getLastName1(),
                        request.getLastName2(),
                        request.getPreferredName()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    public static class CreateEmployeeRequest {
        private String ruleSystemCode;
        private String employeeNumber;
        private String firstName;
        private String lastName1;
        private String lastName2;
        private String preferredName;

        // Getters and setters
        public String getRuleSystemCode() {
            return ruleSystemCode;
        }

        public void setRuleSystemCode(String ruleSystemCode) {
            this.ruleSystemCode = ruleSystemCode;
        }

        public String getEmployeeNumber() {
            return employeeNumber;
        }

        public void setEmployeeNumber(String employeeNumber) {
            this.employeeNumber = employeeNumber;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName1() {
            return lastName1;
        }

        public void setLastName1(String lastName1) {
            this.lastName1 = lastName1;
        }

        public String getLastName2() {
            return lastName2;
        }

        public void setLastName2(String lastName2) {
            this.lastName2 = lastName2;
        }

        public String getPreferredName() {
            return preferredName;
        }

        public void setPreferredName(String preferredName) {
            this.preferredName = preferredName;
        }
    }
}