package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.domain.model.Identifier;

public interface CreateIdentifierUseCase {

    Identifier create(CreateIdentifierCommand command);
}
