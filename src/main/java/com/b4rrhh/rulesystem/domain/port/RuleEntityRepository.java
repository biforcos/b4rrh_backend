package com.b4rrhh.rulesystem.domain.port;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;

import java.util.List;
import java.util.Optional;

public interface RuleEntityRepository {
    Optional<RuleEntity> findById(Long id);
    List<RuleEntity> findAll();
    List<RuleEntity> findByFilters(String ruleSystemCode, String ruleEntityTypeCode, String code, Boolean active);
    Optional<RuleEntity> findByBusinessKey(String ruleSystemCode, String ruleEntityTypeCode, String code);
    RuleEntity save(RuleEntity ruleEntity);
}
