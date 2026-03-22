package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;

public interface GetRuleEntityByBusinessKeyUseCase {
    RuleEntity get(GetRuleEntityByBusinessKeyQuery query);
}
