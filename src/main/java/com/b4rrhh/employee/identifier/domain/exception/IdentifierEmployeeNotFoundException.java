package com.b4rrhh.employee.identifier.domain.exception;

public class IdentifierEmployeeNotFoundException extends RuntimeException {

    public IdentifierEmployeeNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Employee not found with business key ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
