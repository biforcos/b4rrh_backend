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

import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        LaborClassificationController.class,
        LaborClassificationCatalogController.class
})
public class LaborClassificationExceptionHandler {

    @ExceptionHandler({
            LaborClassificationEmployeeNotFoundException.class,
            LaborClassificationNotFoundException.class
    })
    public ResponseEntity<LaborClassificationErrorResponse> handleNotFound(RuntimeException ex) {
        if (ex instanceof LaborClassificationNotFoundException) {
            return notFound(
                    "LABOR_CLASSIFICATION_NOT_FOUND",
                    "No existe la clasificación laboral indicada para el empleado.",
                    null
            );
        }

        return notFound(
                "LABOR_CLASSIFICATION_NOT_FOUND",
                "No se ha encontrado el empleado solicitado para clasificación laboral.",
                null
        );
    }

    @ExceptionHandler({
            LaborClassificationAgreementInvalidException.class,
            LaborClassificationCategoryInvalidException.class,
            LaborClassificationAgreementCategoryRelationInvalidException.class,
            InvalidLaborClassificationDateRangeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<LaborClassificationErrorResponse> handleBadRequest(RuntimeException ex) {
        if (ex instanceof LaborClassificationAgreementInvalidException) {
            return notFound(
                    "AGREEMENT_NOT_FOUND",
                    "El convenio indicado no existe o no está activo para la fecha informada.",
                    Map.of("field", "agreementCode")
            );
        }
        if (ex instanceof LaborClassificationCategoryInvalidException) {
            return notFound(
                    "AGREEMENT_CATEGORY_NOT_FOUND",
                    "La categoría de convenio indicada no existe o no está activa para la fecha informada.",
                    Map.of("field", "agreementCategoryCode")
            );
        }
        if (ex instanceof LaborClassificationAgreementCategoryRelationInvalidException) {
            return conflict(
                    "AGREEMENT_CATEGORY_RELATION_INVALID",
                    "La categoría de convenio no pertenece al convenio indicado para la fecha informada.",
                    null
            );
        }

        return conflict(
                "LABOR_CLASSIFICATION_INVALID_PERIOD",
                "La clasificación laboral es inválida por fechas o datos inconsistentes.",
                null
        );
    }

    @ExceptionHandler({
            LaborClassificationOverlapException.class,
            LaborClassificationOutsidePresencePeriodException.class,
            LaborClassificationCoverageIncompleteException.class,
            LaborClassificationAlreadyClosedException.class
    })
    public ResponseEntity<LaborClassificationErrorResponse> handleConflict(RuntimeException ex) {
        if (ex instanceof LaborClassificationOverlapException) {
            return conflict(
                    "LABOR_CLASSIFICATION_OVERLAP",
                    "El periodo informado se solapa con otra clasificación laboral del empleado.",
                    null
            );
        }
        if (ex instanceof LaborClassificationOutsidePresencePeriodException) {
            return conflict(
                    "LABOR_CLASSIFICATION_OUTSIDE_PRESENCE",
                    "El periodo informado queda fuera de cualquier presencia válida del empleado.",
                    null
            );
        }
        if (ex instanceof LaborClassificationCoverageIncompleteException) {
            return conflict(
                    "LABOR_CLASSIFICATION_INCOMPLETE_COVERAGE",
                    "La operación dejaría huecos en la cobertura de clasificación laboral frente a presence.",
                    null
            );
        }

        return conflict(
                "LABOR_CLASSIFICATION_ALREADY_CLOSED",
                "La clasificación laboral ya estaba cerrada y no puede cerrarse nuevamente.",
                null
        );
    }

    private ResponseEntity<LaborClassificationErrorResponse> notFound(
            String code,
            String message,
            Map<String, Object> details
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new LaborClassificationErrorResponse(code, message, details));
    }

    private ResponseEntity<LaborClassificationErrorResponse> conflict(
            String code,
            String message,
            Map<String, Object> details
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new LaborClassificationErrorResponse(code, message, details));
    }
}
