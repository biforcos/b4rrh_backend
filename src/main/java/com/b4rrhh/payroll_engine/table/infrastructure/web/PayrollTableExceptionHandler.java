package com.b4rrhh.payroll_engine.table.infrastructure.web;

import com.b4rrhh.payroll_engine.table.domain.exception.PayrollTableAlreadyExistsException;
import com.b4rrhh.payroll_engine.table.domain.exception.TableRowAlreadyExistsException;
import com.b4rrhh.payroll_engine.table.domain.exception.TableRowNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        PayrollTableManagementController.class,
        PayrollTableRowManagementController.class
})
public class PayrollTableExceptionHandler {

    @ExceptionHandler(PayrollTableAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleTableAlreadyExists(PayrollTableAlreadyExistsException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(TableRowAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleRowAlreadyExists(TableRowAlreadyExistsException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(TableRowNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleRowNotFound(TableRowNotFoundException e) {
        return Map.of("error", e.getMessage());
    }
}
