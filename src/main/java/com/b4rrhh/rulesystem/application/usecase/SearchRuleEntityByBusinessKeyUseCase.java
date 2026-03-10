package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;

import java.util.Optional;

public interface SearchRuleEntityByBusinessKeyUseCase {
    Optional<RuleEntity> search(String ruleSystemCode, String ruleEntityTypeCode, String code);
}
