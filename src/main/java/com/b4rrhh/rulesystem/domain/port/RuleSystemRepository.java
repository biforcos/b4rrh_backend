package com.b4rrhh.rulesystem.domain.port;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;

import java.util.List;
import java.util.Optional;

public interface RuleSystemRepository {
    Optional<RuleSystem> findByCode(String code);
    List<RuleSystem> findAll();
    RuleSystem save(RuleSystem ruleSystem);
}