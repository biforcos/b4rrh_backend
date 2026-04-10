package com.b4rrhh.authorization.application.usecase;

import com.b4rrhh.authorization.domain.model.PermissionDecision;

public interface EvaluatePermissionUseCase {

    PermissionDecision evaluate(EvaluatePermissionCommand command);
}
