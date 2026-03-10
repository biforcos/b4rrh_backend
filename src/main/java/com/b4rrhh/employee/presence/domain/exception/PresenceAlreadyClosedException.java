package com.b4rrhh.employee.presence.domain.exception;

public class PresenceAlreadyClosedException extends RuntimeException {

    public PresenceAlreadyClosedException(Long presenceId) {
        super("Presence is already closed: " + presenceId);
    }
}
