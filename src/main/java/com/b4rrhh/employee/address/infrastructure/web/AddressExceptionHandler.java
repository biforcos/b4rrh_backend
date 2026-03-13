package com.b4rrhh.employee.address.infrastructure.web;

import com.b4rrhh.employee.address.domain.exception.AddressAlreadyClosedException;
import com.b4rrhh.employee.address.domain.exception.AddressCatalogValueInvalidException;
import com.b4rrhh.employee.address.domain.exception.AddressEmployeeNotFoundException;
import com.b4rrhh.employee.address.domain.exception.AddressNotFoundException;
import com.b4rrhh.employee.address.domain.exception.AddressOverlapException;
import com.b4rrhh.employee.address.domain.exception.AddressRuleSystemNotFoundException;
import com.b4rrhh.employee.address.domain.exception.InvalidAddressDateRangeException;
import com.b4rrhh.employee.address.infrastructure.web.dto.AddressErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AddressBusinessKeyController.class)
public class AddressExceptionHandler {

    @ExceptionHandler({
            AddressEmployeeNotFoundException.class,
            AddressNotFoundException.class,
            AddressRuleSystemNotFoundException.class
    })
    public ResponseEntity<AddressErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new AddressErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            AddressCatalogValueInvalidException.class,
            InvalidAddressDateRangeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<AddressErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AddressErrorResponse(ex.getMessage()));
    }

        @ExceptionHandler({
                        AddressAlreadyClosedException.class,
                        AddressOverlapException.class
        })
        public ResponseEntity<AddressErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AddressErrorResponse(ex.getMessage()));
    }
}
