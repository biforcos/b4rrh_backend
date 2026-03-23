package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminateEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.usecase.TerminateEmployeeUseCase;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.TerminateEmployeeRequest;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.TerminateEmployeeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class TerminateEmployeeController {

    private final TerminateEmployeeUseCase terminateEmployeeUseCase;
    private final TerminateEmployeeWebMapper webMapper;

    public TerminateEmployeeController(
            TerminateEmployeeUseCase terminateEmployeeUseCase,
            TerminateEmployeeWebMapper webMapper
    ) {
        this.terminateEmployeeUseCase = terminateEmployeeUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping("/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/terminate")
    public ResponseEntity<TerminateEmployeeResponse> terminate(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody TerminateEmployeeRequest request
    ) {
        TerminateEmployeeCommand command = webMapper.toCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                request
        );
        TerminateEmployeeResult result = terminateEmployeeUseCase.terminate(command);
        return ResponseEntity.ok(webMapper.toResponse(result));
    }
}
