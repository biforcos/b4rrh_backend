package com.b4rrhh.employee.contract.infrastructure.rest;

import com.b4rrhh.employee.contract.domain.exception.InvalidContractDateRangeException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractAlreadyClosedException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractCoverageIncompleteException;
import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractOutsidePresencePeriodException;
import com.b4rrhh.employee.contract.domain.exception.ContractOverlapException;
import com.b4rrhh.employee.contract.infrastructure.rest.dto.ContractErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ContractController.class)
public class ContractExceptionHandler {

    @ExceptionHandler({
            ContractEmployeeNotFoundException.class,
            ContractNotFoundException.class
    })
    public ResponseEntity<ContractErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ContractErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            ContractInvalidException.class,
            ContractSubtypeInvalidException.class,
            ContractSubtypeRelationInvalidException.class,
            InvalidContractDateRangeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ContractErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ContractErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            ContractOverlapException.class,
            ContractOutsidePresencePeriodException.class,
            ContractCoverageIncompleteException.class,
            ContractAlreadyClosedException.class
    })
    public ResponseEntity<ContractErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ContractErrorResponse(ex.getMessage()));
    }
}
