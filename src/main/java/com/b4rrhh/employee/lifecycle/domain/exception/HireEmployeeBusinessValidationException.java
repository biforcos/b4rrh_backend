package com.b4rrhh.employee.lifecycle.domain.exception;

public class HireEmployeeBusinessValidationException extends RuntimeException {

    public HireEmployeeBusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}