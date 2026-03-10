package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;

import java.util.Optional;

public interface GetRuleEntityByIdUseCase {
    Optional<RuleEntity> getById(Long id);
}
