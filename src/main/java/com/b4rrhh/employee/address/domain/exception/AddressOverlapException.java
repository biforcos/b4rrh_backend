package com.b4rrhh.employee.address.domain.exception;

public class AddressOverlapException extends RuntimeException {

    public AddressOverlapException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String addressTypeCode
    ) {
        super("Address period overlaps for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", addressTypeCode="
                + addressTypeCode);
    }
}
