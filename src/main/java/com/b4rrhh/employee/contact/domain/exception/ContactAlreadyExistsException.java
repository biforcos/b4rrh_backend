package com.b4rrhh.employee.contact.domain.exception;

public class ContactAlreadyExistsException extends RuntimeException {

    public ContactAlreadyExistsException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String contactTypeCode
    ) {
        super("Contact already exists for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", contactTypeCode="
                + contactTypeCode);
    }
}
