package com.b4rrhh.employee.presence.infrastructure.web;

import com.b4rrhh.employee.presence.domain.exception.ActivePresenceAlreadyExistsException;
import com.b4rrhh.employee.presence.domain.exception.InvalidPresenceDateRangeException;
import com.b4rrhh.employee.presence.domain.exception.PresenceAlreadyClosedException;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceOverlapException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.employee.presence.infrastructure.web.dto.PresenceErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PresenceBusinessKeyController.class)
public class PresenceExceptionHandler {

    @ExceptionHandler({
            PresenceEmployeeNotFoundException.class,
            PresenceNotFoundException.class,
            PresenceRuleSystemNotFoundException.class
    })
    public ResponseEntity<PresenceErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new PresenceErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            PresenceCatalogValueInvalidException.class,
            InvalidPresenceDateRangeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<PresenceErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PresenceErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            PresenceOverlapException.class,
            ActivePresenceAlreadyExistsException.class,
            PresenceAlreadyClosedException.class
    })
    public ResponseEntity<PresenceErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new PresenceErrorResponse(ex.getMessage()));
    }
}
