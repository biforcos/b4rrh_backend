package com.b4rrhh.employee.identifier.domain.exception;

public class IdentifierAlreadyExistsException extends RuntimeException {

    public IdentifierAlreadyExistsException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String identifierTypeCode
    ) {
        super("Identifier already exists for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", identifierTypeCode="
                + identifierTypeCode);
    }
}
