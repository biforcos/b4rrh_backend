package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;

import java.util.Optional;

public interface GetRuleSystemByCodeUseCase {
    Optional<RuleSystem> getByCode(String code);
}