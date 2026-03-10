package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntityType;

public interface CreateRuleEntityTypeUseCase {
    RuleEntityType create(CreateRuleEntityTypeCommand command);
}
