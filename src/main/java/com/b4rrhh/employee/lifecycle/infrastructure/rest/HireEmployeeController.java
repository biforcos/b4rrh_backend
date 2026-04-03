package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.usecase.HireEmployeeUseCase;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HireEmployeeRequest;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HireEmployeeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class HireEmployeeController {

    private final HireEmployeeUseCase hireEmployeeUseCase;
    private final HireEmployeeWebMapper webMapper;

    public HireEmployeeController(
            HireEmployeeUseCase hireEmployeeUseCase,
            HireEmployeeWebMapper webMapper
    ) {
        this.hireEmployeeUseCase = hireEmployeeUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping("/hire")
    public ResponseEntity<HireEmployeeResponse> hire(@RequestBody HireEmployeeRequest request) {
        HireEmployeeCommand command = webMapper.toCommand(request);
        HireEmployeeResult result = hireEmployeeUseCase.hire(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(result));
    }
}
