package com.b4rrhh.payroll_engine.eligibility.infrastructure.web;

import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptNotFoundException;
import com.b4rrhh.payroll_engine.eligibility.domain.exception.ConceptAssignmentNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps eligibility-side domain exceptions to HTTP status codes for the assignment
 * management endpoints. Ordered ahead of the broader concept-side handler so that
 * {@link ConceptAssignmentNotFoundException} resolves to 404 before any wider matcher
 * intercepts it.
 */
@RestControllerAdvice(basePackages = "com.b4rrhh.payroll_engine")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConceptAssignmentManagementExceptionHandler {

    @ExceptionHandler(ConceptAssignmentNotFoundException.class)
    public ResponseEntity<ConceptAssignmentErrorResponse> handleNotFound(
            ConceptAssignmentNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ConceptAssignmentErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PayrollConceptNotFoundException.class)
    public ResponseEntity<ConceptAssignmentErrorResponse> handleConceptNotFound(
            PayrollConceptNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ConceptAssignmentErrorResponse(ex.getMessage()));
    }
}
