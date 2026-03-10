package com.b4rrhh.employee.presence.domain.exception;

public class PresenceOverlapException extends RuntimeException {

    public PresenceOverlapException(Long employeeId) {
        super("Presence period overlaps existing period for employee: " + employeeId);
    }
}
