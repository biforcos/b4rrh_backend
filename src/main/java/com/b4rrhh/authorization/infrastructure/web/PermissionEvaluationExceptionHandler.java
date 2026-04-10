package com.b4rrhh.authorization.infrastructure.web;

import com.b4rrhh.authorization.domain.exception.SecuredResourceNotFoundException;
import com.b4rrhh.authorization.infrastructure.web.dto.AuthorizationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PermissionEvaluationBusinessKeyController.class)
public class PermissionEvaluationExceptionHandler {

    @ExceptionHandler(SecuredResourceNotFoundException.class)
    public ResponseEntity<AuthorizationErrorResponse> handleNotFound(SecuredResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new AuthorizationErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthorizationErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthorizationErrorResponse(ex.getMessage()));
    }
}
