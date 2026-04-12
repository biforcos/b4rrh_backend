package com.b4rrhh.payroll.infrastructure.web;

import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;
import com.b4rrhh.payroll.domain.exception.PayrollBusinessKeyConflictException;
import com.b4rrhh.payroll.domain.exception.PayrollEmployeePresenceNotFoundException;
import com.b4rrhh.payroll.domain.exception.PayrollInvalidStateTransitionException;
import com.b4rrhh.payroll.domain.exception.PayrollNotFoundException;
import com.b4rrhh.payroll.domain.exception.PayrollRecalculationNotAllowedException;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {PayrollController.class, PayrollCalculationRunController.class})
public class PayrollExceptionHandler {

    @ExceptionHandler({
            PayrollNotFoundException.class,
            PayrollEmployeePresenceNotFoundException.class
    })
    public ResponseEntity<PayrollErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PayrollErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            InvalidPayrollArgumentException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<PayrollErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PayrollErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            PayrollInvalidStateTransitionException.class,
            PayrollRecalculationNotAllowedException.class,
            PayrollBusinessKeyConflictException.class
    })
    public ResponseEntity<PayrollErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new PayrollErrorResponse(ex.getMessage()));
    }
}