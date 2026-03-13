package com.b4rrhh.employee.presence.domain.exception;

public class ActivePresenceAlreadyExistsException extends RuntimeException {

    public ActivePresenceAlreadyExistsException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("An active presence already exists for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
