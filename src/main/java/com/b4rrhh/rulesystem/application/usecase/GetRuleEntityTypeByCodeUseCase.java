package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntityType;

import java.util.Optional;

public interface GetRuleEntityTypeByCodeUseCase {
    Optional<RuleEntityType> getByCode(String code);
}
