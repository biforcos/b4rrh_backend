package com.b4rrhh.employee.presence.domain.exception;

public class ActivePresenceAlreadyExistsException extends RuntimeException {

    public ActivePresenceAlreadyExistsException(Long employeeId) {
        super("An active presence already exists for employee: " + employeeId);
    }
}
