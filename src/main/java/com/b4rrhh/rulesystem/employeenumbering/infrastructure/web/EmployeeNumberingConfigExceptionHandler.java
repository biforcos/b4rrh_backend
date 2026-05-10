package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web;

import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigInvalidException;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto.EmployeeNumberingConfigErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmployeeNumberingConfigController.class)
public class EmployeeNumberingConfigExceptionHandler {

    @ExceptionHandler(EmployeeNumberingConfigInvalidException.class)
    public ResponseEntity<EmployeeNumberingConfigErrorResponse> handleInvalid(EmployeeNumberingConfigInvalidException ex) {
        return ResponseEntity.unprocessableEntity().body(new EmployeeNumberingConfigErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.notFound().build();
    }
}
