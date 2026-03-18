package com.b4rrhh.employee.cost_center.infrastructure.rest;

import com.b4rrhh.employee.cost_center.domain.exception.CostCenterAllocationNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterAlreadyClosedException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOverlapException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterRuleSystemNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.InvalidAllocationPercentageException;
import com.b4rrhh.employee.cost_center.domain.exception.InvalidCostCenterDateRangeException;
import com.b4rrhh.employee.cost_center.infrastructure.rest.dto.CostCenterErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CostCenterController.class)
public class CostCenterExceptionHandler {

    @ExceptionHandler({
            CostCenterEmployeeNotFoundException.class,
            CostCenterAllocationNotFoundException.class,
            CostCenterRuleSystemNotFoundException.class
    })
    public ResponseEntity<CostCenterErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CostCenterErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            CostCenterCatalogValueInvalidException.class,
            InvalidCostCenterDateRangeException.class,
            InvalidAllocationPercentageException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<CostCenterErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CostCenterErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            CostCenterAlreadyClosedException.class,
            CostCenterOverlapException.class,
            CostCenterOutsidePresencePeriodException.class
    })
    public ResponseEntity<CostCenterErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CostCenterErrorResponse(ex.getMessage()));
    }
}
