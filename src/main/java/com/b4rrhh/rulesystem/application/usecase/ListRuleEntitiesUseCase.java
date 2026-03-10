package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;

import java.util.List;

public interface ListRuleEntitiesUseCase {
    List<RuleEntity> list(ListRuleEntitiesQuery query);
}
