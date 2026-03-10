package com.b4rrhh.rulesystem.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataRuleEntityRepository extends JpaRepository<RuleEntityEntity, Long> {

    @Query("""
        select re
        from RuleEntityEntity re
        where (:ruleSystemCode is null or re.ruleSystemCode = :ruleSystemCode)
          and (:ruleEntityTypeCode is null or re.ruleEntityTypeCode = :ruleEntityTypeCode)
          and (:code is null or re.code = :code)
          and (:active is null or re.active = :active)
        order by re.ruleSystemCode, re.ruleEntityTypeCode, re.code
        """)
    List<RuleEntityEntity> findByFilters(
        @Param("ruleSystemCode") String ruleSystemCode,
        @Param("ruleEntityTypeCode") String ruleEntityTypeCode,
        @Param("code") String code,
        @Param("active") Boolean active
    );

    Optional<RuleEntityEntity> findByRuleSystemCodeAndRuleEntityTypeCodeAndCode(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code
    );
}
