package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;

import java.util.List;

public interface ListRuleSystemsUseCase {
    List<RuleSystem> listAll();
}