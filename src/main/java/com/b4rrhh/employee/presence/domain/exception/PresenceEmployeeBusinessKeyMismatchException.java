package com.b4rrhh.employee.presence.domain.exception;

public class PresenceEmployeeBusinessKeyMismatchException extends RuntimeException {

    public PresenceEmployeeBusinessKeyMismatchException(
            String ruleSystemCode,
            String employeeNumber,
            String employeeRuleSystemCode
    ) {
        super("Employee business key mismatch for ruleSystemCode="
                + ruleSystemCode
                + " and employeeNumber="
                + employeeNumber
                + ". Stored ruleSystemCode="
                + employeeRuleSystemCode);
    }
}
