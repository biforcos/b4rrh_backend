package com.b4rrhh.employee.contact.domain.exception;

public class ContactNotFoundException extends RuntimeException {

    public ContactNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String contactTypeCode
    ) {
        super("Contact not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", contactTypeCode="
                + contactTypeCode);
    }
}
