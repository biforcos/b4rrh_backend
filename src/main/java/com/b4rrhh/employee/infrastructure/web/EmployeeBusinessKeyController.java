package com.b4rrhh.employee.infrastructure.web;

import com.b4rrhh.employee.application.usecase.GetEmployeeByBusinessKeyUseCase;
import com.b4rrhh.employee.domain.model.Employee;
import com.b4rrhh.employee.infrastructure.web.dto.EmployeeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeBusinessKeyController {

    private final GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase;

    public EmployeeBusinessKeyController(GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase) {
        this.getEmployeeByBusinessKeyUseCase = getEmployeeByBusinessKeyUseCase;
    }

    @GetMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}")
    public ResponseEntity<EmployeeResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        return getEmployeeByBusinessKeyUseCase.getByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .map(employee -> ResponseEntity.ok(toResponse(employee)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
