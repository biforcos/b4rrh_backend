package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptAlreadyExistsException;
import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PayrollConceptManagementController.class)
public class PayrollConceptManagementExceptionHandler {

    @ExceptionHandler(PayrollConceptAlreadyExistsException.class)
    public ResponseEntity<PayrollConceptErrorResponse> handleAlreadyExists(PayrollConceptAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new PayrollConceptErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PayrollConceptNotFoundException.class)
    public ResponseEntity<PayrollConceptErrorResponse> handleNotFound(PayrollConceptNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new PayrollConceptErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PayrollConceptErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PayrollConceptErrorResponse(ex.getMessage()));
    }
}
