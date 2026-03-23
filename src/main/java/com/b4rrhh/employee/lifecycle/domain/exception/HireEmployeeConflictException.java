package com.b4rrhh.employee.lifecycle.domain.exception;

public class HireEmployeeConflictException extends RuntimeException {

    public HireEmployeeConflictException(String message) {
        super(message);
    }
}