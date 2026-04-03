package com.b4rrhh.employee.lifecycle.domain.exception;

public class RehireEmployeeDistributionInvalidException extends RuntimeException {

    public RehireEmployeeDistributionInvalidException(String message) {
        super(message);
    }

    public RehireEmployeeDistributionInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
