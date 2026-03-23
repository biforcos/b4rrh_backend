package com.b4rrhh.employee.lifecycle.domain.exception;

public class RehireEmployeeEmployeeNotFoundException extends RuntimeException {

    public RehireEmployeeEmployeeNotFoundException(
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