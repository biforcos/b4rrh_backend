package com.b4rrhh.employee.cost_center.infrastructure.web;

import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionConflictException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionPercentageExceededException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionStartDateMismatchException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.exception.InvalidAllocationPercentageException;
import com.b4rrhh.employee.cost_center.domain.exception.InvalidCostCenterDateRangeException;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CostCenterBusinessKeyController.class)
public class CostCenterExceptionHandler {

    @ExceptionHandler({
            CostCenterEmployeeNotFoundException.class,
            CostCenterDistributionNotFoundException.class
    })
    public ResponseEntity<CostCenterErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CostCenterErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            CostCenterCatalogValueInvalidException.class,
            InvalidCostCenterDateRangeException.class,
            InvalidAllocationPercentageException.class,
            CostCenterDistributionInvalidException.class,
            CostCenterDistributionPercentageExceededException.class,
            CostCenterDistributionStartDateMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<CostCenterErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CostCenterErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            CostCenterDistributionConflictException.class,
            CostCenterOutsidePresencePeriodException.class
    })
    public ResponseEntity<CostCenterErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CostCenterErrorResponse(ex.getMessage()));
    }
}
