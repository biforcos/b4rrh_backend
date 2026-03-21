package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.domain.exception.RuleSystemNotFoundException;
import com.b4rrhh.rulesystem.infrastructure.web.dto.RuleSystemErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RuleSystemController.class)
public class RuleSystemExceptionHandler {

    @ExceptionHandler(RuleSystemNotFoundException.class)
    public ResponseEntity<RuleSystemErrorResponse> handleNotFound(RuleSystemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RuleSystemErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RuleSystemErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RuleSystemErrorResponse(ex.getMessage()));
    }
}
