package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.employee.domain.exception.EmployeeRuleSystemNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationEmployeeNotFoundException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeEmployeeNotFoundException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeRequestInvalidException;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.TerminateEmployeeErrorResponse;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterEmployeeNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterRuleSystemNotFoundException;
import com.b4rrhh.rulesystem.domain.exception.RuleSystemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TerminateEmployeeController.class)
public class TerminateEmployeeExceptionHandler {

    @ExceptionHandler(TerminateEmployeeRequestInvalidException.class)
    public ResponseEntity<TerminateEmployeeErrorResponse> handleInvalidRequest(TerminateEmployeeRequestInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new TerminateEmployeeErrorResponse("TERMINATE_REQUEST_INVALID", ex.getMessage(), null));
    }

    @ExceptionHandler(TerminateEmployeeCatalogValueInvalidException.class)
    public ResponseEntity<TerminateEmployeeErrorResponse> handleCatalogInvalid(TerminateEmployeeCatalogValueInvalidException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new TerminateEmployeeErrorResponse("INVALID_CATALOG_VALUE", ex.getMessage(), null));
    }

    @ExceptionHandler(TerminateEmployeeConflictException.class)
    public ResponseEntity<TerminateEmployeeErrorResponse> handleConflict(TerminateEmployeeConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new TerminateEmployeeErrorResponse("TERMINATE_CONFLICT", ex.getMessage(), null));
    }

    @ExceptionHandler({
            TerminateEmployeeEmployeeNotFoundException.class,
            RuleSystemNotFoundException.class,
            EmployeeRuleSystemNotFoundException.class,
            PresenceRuleSystemNotFoundException.class,
            PresenceEmployeeNotFoundException.class,
            LaborClassificationEmployeeNotFoundException.class,
            ContractEmployeeNotFoundException.class,
            WorkCenterRuleSystemNotFoundException.class,
            WorkCenterEmployeeNotFoundException.class
    })
    public ResponseEntity<TerminateEmployeeErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new TerminateEmployeeErrorResponse("TERMINATE_DEPENDENCY_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TerminateEmployeeErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new TerminateEmployeeErrorResponse("TERMINATE_REQUEST_INVALID", ex.getMessage(), null));
    }
}
