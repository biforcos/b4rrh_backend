package com.b4rrhh.employee.infrastructure.web;

import com.b4rrhh.employee.domain.exception.EmployeeRuleSystemNotFoundException;
import com.b4rrhh.employee.infrastructure.web.dto.EmployeeErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {EmployeeController.class, EmployeeBusinessKeyController.class})
public class EmployeeExceptionHandler {

    @ExceptionHandler(EmployeeRuleSystemNotFoundException.class)
    public ResponseEntity<EmployeeErrorResponse> handleRuleSystemNotFound(EmployeeRuleSystemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new EmployeeErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EmployeeErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new EmployeeErrorResponse(ex.getMessage()));
    }
}
