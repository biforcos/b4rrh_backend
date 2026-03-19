package com.b4rrhh.employee.labor_classification.infrastructure.rest;

import com.b4rrhh.employee.labor_classification.domain.exception.InvalidLaborClassificationDateRangeException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAlreadyClosedException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCategoryInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCoverageIncompleteException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationEmployeeNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOutsidePresencePeriodException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOverlapException;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.LaborClassificationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = LaborClassificationController.class)
public class LaborClassificationExceptionHandler {

    @ExceptionHandler({
            LaborClassificationEmployeeNotFoundException.class,
            LaborClassificationNotFoundException.class
    })
    public ResponseEntity<LaborClassificationErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new LaborClassificationErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            LaborClassificationAgreementInvalidException.class,
            LaborClassificationCategoryInvalidException.class,
            LaborClassificationAgreementCategoryRelationInvalidException.class,
            InvalidLaborClassificationDateRangeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<LaborClassificationErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new LaborClassificationErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            LaborClassificationOverlapException.class,
            LaborClassificationOutsidePresencePeriodException.class,
            LaborClassificationCoverageIncompleteException.class,
            LaborClassificationAlreadyClosedException.class
    })
    public ResponseEntity<LaborClassificationErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new LaborClassificationErrorResponse(ex.getMessage()));
    }
}
