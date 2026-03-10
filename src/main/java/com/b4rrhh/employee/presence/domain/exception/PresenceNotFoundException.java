package com.b4rrhh.employee.presence.domain.exception;

public class PresenceNotFoundException extends RuntimeException {

    public PresenceNotFoundException(Long employeeId, Long presenceId) {
        super("Presence not found for employeeId=" + employeeId + " and presenceId=" + presenceId);
    }
}
