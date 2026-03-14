package com.b4rrhh.employee.identifier.infrastructure.web;

import com.b4rrhh.employee.identifier.domain.exception.IdentifierAlreadyExistsException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierCatalogValueInvalidException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierPrimaryAlreadyExistsException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierRuleSystemNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierValueInvalidException;
import com.b4rrhh.employee.identifier.infrastructure.web.dto.IdentifierErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = IdentifierController.class)
public class IdentifierExceptionHandler {

    @ExceptionHandler({
            IdentifierEmployeeNotFoundException.class,
            IdentifierNotFoundException.class,
            IdentifierRuleSystemNotFoundException.class
    })
    public ResponseEntity<IdentifierErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new IdentifierErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            IdentifierCatalogValueInvalidException.class,
            IdentifierValueInvalidException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<IdentifierErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new IdentifierErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            IdentifierAlreadyExistsException.class,
            IdentifierPrimaryAlreadyExistsException.class
    })
    public ResponseEntity<IdentifierErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new IdentifierErrorResponse(ex.getMessage()));
    }
}
