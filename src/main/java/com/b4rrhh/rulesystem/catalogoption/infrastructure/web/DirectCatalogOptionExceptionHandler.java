package com.b4rrhh.rulesystem.catalogoption.infrastructure.web;

import com.b4rrhh.rulesystem.catalogoption.infrastructure.web.dto.DirectCatalogOptionErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = DirectCatalogOptionController.class)
public class DirectCatalogOptionExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<DirectCatalogOptionErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DirectCatalogOptionErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<DirectCatalogOptionErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DirectCatalogOptionErrorResponse("Invalid parameter: " + ex.getName()));
    }
}
