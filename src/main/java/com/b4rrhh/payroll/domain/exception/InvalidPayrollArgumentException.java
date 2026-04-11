package com.b4rrhh.payroll.domain.exception;

public class InvalidPayrollArgumentException extends RuntimeException {

    public InvalidPayrollArgumentException(String message) {
        super(message);
    }
}