package com.b4rrhh.employee.journey.infrastructure.web;

import com.b4rrhh.employee.journey.application.usecase.JourneyEmployeeNotFoundException;
import com.b4rrhh.employee.journey.infrastructure.web.dto.JourneyErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = JourneyV2Controller.class)
public class JourneyV2ExceptionHandler {

    @ExceptionHandler(JourneyEmployeeNotFoundException.class)
    public ResponseEntity<JourneyErrorResponse> handleNotFound(JourneyEmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new JourneyErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JourneyErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JourneyErrorResponse(ex.getMessage()));
    }
}