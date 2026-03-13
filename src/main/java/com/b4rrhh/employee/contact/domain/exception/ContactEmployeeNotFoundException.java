package com.b4rrhh.employee.contact.domain.exception;

public class ContactEmployeeNotFoundException extends RuntimeException {

    public ContactEmployeeNotFoundException(
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
