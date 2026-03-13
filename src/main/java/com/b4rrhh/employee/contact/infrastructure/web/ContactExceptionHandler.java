package com.b4rrhh.employee.contact.infrastructure.web;

import com.b4rrhh.employee.contact.domain.exception.ContactAlreadyExistsException;
import com.b4rrhh.employee.contact.domain.exception.ContactCatalogValueInvalidException;
import com.b4rrhh.employee.contact.domain.exception.ContactEmployeeNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactRuleSystemNotFoundException;
import com.b4rrhh.employee.contact.domain.exception.ContactTypeMutationNotAllowedException;
import com.b4rrhh.employee.contact.domain.exception.ContactValueInvalidException;
import com.b4rrhh.employee.contact.infrastructure.web.dto.ContactErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ContactController.class)
public class ContactExceptionHandler {

    @ExceptionHandler({
            ContactEmployeeNotFoundException.class,
            ContactNotFoundException.class,
            ContactRuleSystemNotFoundException.class
    })
    public ResponseEntity<ContactErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ContactErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            ContactCatalogValueInvalidException.class,
            ContactValueInvalidException.class,
            ContactTypeMutationNotAllowedException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ContactErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ContactErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ContactAlreadyExistsException.class)
    public ResponseEntity<ContactErrorResponse> handleConflict(ContactAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ContactErrorResponse(ex.getMessage()));
    }
}
