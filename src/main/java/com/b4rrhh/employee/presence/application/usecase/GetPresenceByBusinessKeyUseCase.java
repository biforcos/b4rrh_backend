package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.domain.model.Presence;

import java.util.Optional;

public interface GetPresenceByBusinessKeyUseCase {

    Optional<Presence> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber
    );
}
