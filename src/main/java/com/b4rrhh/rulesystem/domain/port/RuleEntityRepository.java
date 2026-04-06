package com.b4rrhh.rulesystem.domain.port;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RuleEntityRepository {
    List<RuleEntity> findAll();
    List<RuleEntity> findByFilters(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            Boolean active,
            LocalDate referenceDate
    );
    Optional<RuleEntity> findApplicableByBusinessKey(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate referenceDate
    );
    Optional<RuleEntity> findByBusinessKey(String ruleSystemCode, String ruleEntityTypeCode, String code);
    Optional<RuleEntity> findByBusinessKeyAndStartDate(String ruleSystemCode, String ruleEntityTypeCode, String code, LocalDate startDate);
    boolean existsOverlapExcludingStartDate(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate projectedStartDate,
            LocalDate projectedEndDate,
            LocalDate excludedStartDate
    );
    void deleteByBusinessKeyAndStartDate(String ruleSystemCode, String ruleEntityTypeCode, String code, LocalDate startDate);
    RuleEntity save(RuleEntity ruleEntity);
}
