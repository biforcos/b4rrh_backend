package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;

public interface UpdateRuleSystemUseCase {
    RuleSystem execute(UpdateRuleSystemCommand command);
}
