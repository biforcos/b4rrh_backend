package com.b4rrhh.employee.presence.domain.exception;

public class PresenceOverlapException extends RuntimeException {

    public PresenceOverlapException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Presence period overlaps an existing period for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
