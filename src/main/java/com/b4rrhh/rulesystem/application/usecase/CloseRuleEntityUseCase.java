package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;

public interface CloseRuleEntityUseCase {
    RuleEntity close(CloseRuleEntityCommand command);
}
