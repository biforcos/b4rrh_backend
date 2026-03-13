package com.b4rrhh.employee.address.domain.exception;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer addressNumber
    ) {
        super("Address not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", addressNumber="
                + addressNumber);
    }
}
