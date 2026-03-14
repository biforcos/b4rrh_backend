package com.b4rrhh.employee.identifier.domain.exception;

public class IdentifierNotFoundException extends RuntimeException {

    public IdentifierNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String identifierTypeCode
    ) {
        super("Identifier not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", identifierTypeCode="
                + identifierTypeCode);
    }
}
