package com.b4rrhh.employee.presence.domain.exception;

public class PresenceNotFoundException extends RuntimeException {

    public PresenceNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber
    ) {
        super("Presence not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", presenceNumber="
                + presenceNumber);
    }
}
