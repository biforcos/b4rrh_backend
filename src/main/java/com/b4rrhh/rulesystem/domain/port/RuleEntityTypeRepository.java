package com.b4rrhh.rulesystem.domain.port;

import com.b4rrhh.rulesystem.domain.model.RuleEntityType;

import java.util.List;
import java.util.Optional;

public interface RuleEntityTypeRepository {
    Optional<RuleEntityType> findByCode(String code);
    List<RuleEntityType> findAll();
    RuleEntityType save(RuleEntityType ruleEntityType);
}
