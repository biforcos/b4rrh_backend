package com.b4rrhh.employee.presence.domain.exception;

public class PresenceAlreadyClosedException extends RuntimeException {

    public PresenceAlreadyClosedException(Integer presenceNumber) {
        super("Presence is already closed for presenceNumber=" + presenceNumber);
    }
}
