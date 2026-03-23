package com.b4rrhh.employee.lifecycle.domain.exception;

public class HireEmployeeAlreadyExistsException extends RuntimeException {

    public HireEmployeeAlreadyExistsException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Employee already exists for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
