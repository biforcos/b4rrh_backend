package com.b4rrhh.employee.identifier.domain.exception;

public class IdentifierPrimaryAlreadyExistsException extends RuntimeException {

    public IdentifierPrimaryAlreadyExistsException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Primary identifier already exists for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
