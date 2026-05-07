package com.b4rrhh.employee.tax_information.infrastructure.web;

import com.b4rrhh.employee.tax_information.domain.exception.*;
import com.b4rrhh.employee.tax_information.infrastructure.web.dto.EmployeeTaxInformationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmployeeTaxInformationBusinessKeyController.class)
public class EmployeeTaxInformationExceptionHandler {

    @ExceptionHandler({EmployeeTaxInformationNotFoundException.class, EmployeeTaxInformationEmployeeNotFoundException.class})
    public ResponseEntity<EmployeeTaxInformationErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new EmployeeTaxInformationErrorResponse("TAX_INFORMATION_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(EmployeeTaxInformationAlreadyExistsException.class)
    public ResponseEntity<EmployeeTaxInformationErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new EmployeeTaxInformationErrorResponse("TAX_INFORMATION_ALREADY_EXISTS", ex.getMessage(), null));
    }

    @ExceptionHandler({EmployeeTaxInformationInvalidValidFromException.class, IllegalArgumentException.class})
    public ResponseEntity<EmployeeTaxInformationErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new EmployeeTaxInformationErrorResponse("TAX_INFORMATION_INVALID_INPUT", ex.getMessage(), null));
    }
}
