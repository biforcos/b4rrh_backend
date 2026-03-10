package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.domain.model.Presence;

public interface ClosePresenceUseCase {
    Presence close(ClosePresenceCommand command);
}
