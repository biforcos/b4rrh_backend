package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.domain.model.Presence;

import java.util.Optional;

public interface GetPresenceByIdUseCase {
    Optional<Presence> getById(Long employeeId, Long presenceId);
}
