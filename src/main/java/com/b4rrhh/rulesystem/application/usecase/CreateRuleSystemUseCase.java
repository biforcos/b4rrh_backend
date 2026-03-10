package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;

public interface CreateRuleSystemUseCase {
    RuleSystem create(CreateRuleSystemCommand command);
}