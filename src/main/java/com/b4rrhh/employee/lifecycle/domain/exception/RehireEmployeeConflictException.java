package com.b4rrhh.employee.lifecycle.domain.exception;

public class RehireEmployeeConflictException extends RuntimeException {

    public RehireEmployeeConflictException(String message) {
        super(message);
    }

    public RehireEmployeeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}