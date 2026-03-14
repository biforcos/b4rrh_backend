package com.b4rrhh.employee.workcenter.infrastructure.web;

import com.b4rrhh.employee.workcenter.domain.exception.InvalidWorkCenterDateRangeException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterAlreadyClosedException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterEmployeeNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterPresenceCoverageGapException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterRuleSystemNotFoundException;
import com.b4rrhh.employee.workcenter.infrastructure.web.dto.WorkCenterErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WorkCenterController.class)
public class WorkCenterExceptionHandler {

    @ExceptionHandler({
            WorkCenterEmployeeNotFoundException.class,
            WorkCenterNotFoundException.class,
            WorkCenterRuleSystemNotFoundException.class
    })
    public ResponseEntity<WorkCenterErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new WorkCenterErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            WorkCenterCatalogValueInvalidException.class,
            InvalidWorkCenterDateRangeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<WorkCenterErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new WorkCenterErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            WorkCenterAlreadyClosedException.class,
            WorkCenterOverlapException.class,
            WorkCenterOutsidePresencePeriodException.class,
            WorkCenterPresenceCoverageGapException.class
    })
    public ResponseEntity<WorkCenterErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new WorkCenterErrorResponse(ex.getMessage()));
    }
}