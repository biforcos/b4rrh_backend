package com.b4rrhh.employee.lifecycle.domain.exception;

public class TerminateEmployeeEmployeeNotFoundException extends RuntimeException {

    public TerminateEmployeeEmployeeNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Employee not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
