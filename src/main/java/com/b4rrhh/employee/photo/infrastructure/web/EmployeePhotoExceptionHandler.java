package com.b4rrhh.employee.photo.infrastructure.web;

import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.infrastructure.web.dto.EmployeePhotoErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmployeePhotoController.class)
public class EmployeePhotoExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundForPhotoException.class)
    public ResponseEntity<EmployeePhotoErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundForPhotoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new EmployeePhotoErrorResponse("EMPLOYEE_NOT_FOUND", ex.getMessage()));
    }
}
