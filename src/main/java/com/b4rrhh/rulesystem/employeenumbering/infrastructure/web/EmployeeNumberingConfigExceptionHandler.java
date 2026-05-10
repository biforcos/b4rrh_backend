package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web;

import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigInvalidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmployeeNumberingConfigController.class)
public class EmployeeNumberingConfigExceptionHandler {

    @ExceptionHandler(EmployeeNumberingConfigInvalidException.class)
    public ResponseEntity<String> handleInvalid(EmployeeNumberingConfigInvalidException ex) {
        return ResponseEntity.unprocessableEntity().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.notFound().build();
    }
}
