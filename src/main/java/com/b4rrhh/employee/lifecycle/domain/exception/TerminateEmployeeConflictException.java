package com.b4rrhh.employee.lifecycle.domain.exception;

public class TerminateEmployeeConflictException extends RuntimeException {

    public TerminateEmployeeConflictException(String message) {
        super(message);
    }

    public TerminateEmployeeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
