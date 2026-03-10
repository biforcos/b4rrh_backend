package com.b4rrhh.employee.presence.domain.exception;

public class PresenceEmployeeNotFoundException extends RuntimeException {

    public PresenceEmployeeNotFoundException(Long employeeId) {
        super("Employee not found with id: " + employeeId);
    }

    public PresenceEmployeeNotFoundException(String ruleSystemCode, String employeeNumber) {
        super("Employee not found with business key ruleSystemCode="
                + ruleSystemCode
                + " and employeeNumber="
                + employeeNumber);
    }
}
