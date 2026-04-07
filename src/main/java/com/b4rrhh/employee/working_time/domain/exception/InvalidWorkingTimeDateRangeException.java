package com.b4rrhh.employee.working_time.domain.exception;

public class InvalidWorkingTimeDateRangeException extends RuntimeException {

    public InvalidWorkingTimeDateRangeException(String message) {
        super(message);
    }
}