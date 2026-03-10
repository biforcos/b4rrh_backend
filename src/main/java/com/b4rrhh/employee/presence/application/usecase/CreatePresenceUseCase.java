package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.domain.model.Presence;

public interface CreatePresenceUseCase {
    Presence create(CreatePresenceCommand command);
}
