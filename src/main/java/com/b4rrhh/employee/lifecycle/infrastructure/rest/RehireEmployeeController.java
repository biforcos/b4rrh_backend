package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.RehireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.RehireEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.usecase.RehireEmployeeUseCase;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehireEmployeeRequest;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehireEmployeeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class RehireEmployeeController {

    private final RehireEmployeeUseCase rehireEmployeeUseCase;
    private final RehireEmployeeWebMapper webMapper;

    public RehireEmployeeController(
            RehireEmployeeUseCase rehireEmployeeUseCase,
            RehireEmployeeWebMapper webMapper
    ) {
        this.rehireEmployeeUseCase = rehireEmployeeUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping("/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/rehire")
    public ResponseEntity<RehireEmployeeResponse> rehire(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody RehireEmployeeRequest request
    ) {
        RehireEmployeeCommand command = webMapper.toCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                request
        );
        RehireEmployeeResult result = rehireEmployeeUseCase.rehire(command);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(webMapper.toResponse(result));
    }
}
