package com.b4rrhh.rulesystem.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataRuleEntityRepository extends JpaRepository<RuleEntityEntity, Long> {

    LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    @Query("""
        select re
        from RuleEntityEntity re
        where (:ruleSystemCode is null or re.ruleSystemCode = :ruleSystemCode)
          and (:ruleEntityTypeCode is null or re.ruleEntityTypeCode = :ruleEntityTypeCode)
          and (:code is null or re.code = :code)
          and (:active is null or re.active = :active)
          and (:referenceDate is null or re.startDate <= :referenceDate)
          and (:referenceDate is null or :referenceDate <= coalesce(re.endDate, :maxDate))
        order by re.ruleSystemCode, re.ruleEntityTypeCode, re.code
        """)
    List<RuleEntityEntity> findByFilters(
        @Param("ruleSystemCode") String ruleSystemCode,
        @Param("ruleEntityTypeCode") String ruleEntityTypeCode,
        @Param("code") String code,
        @Param("active") Boolean active,
        @Param("referenceDate") LocalDate referenceDate,
        @Param("maxDate") LocalDate maxDate
    );

    @Query("""
        select re
        from RuleEntityEntity re
        where re.ruleSystemCode = :ruleSystemCode
          and re.ruleEntityTypeCode = :ruleEntityTypeCode
          and re.active = true
          and (:referenceDate is null or re.startDate <= :referenceDate)
          and (:referenceDate is null or :referenceDate <= coalesce(re.endDate, :maxDate))
          and (
              :qLike is null
              or lower(re.code) like :qLike
              or lower(re.name) like :qLike
          )
        order by lower(coalesce(re.name, '')), re.code
        """)
    List<RuleEntityEntity> findDirectCatalogOptions(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("ruleEntityTypeCode") String ruleEntityTypeCode,
            @Param("referenceDate") LocalDate referenceDate,
            @Param("qLike") String qLike,
            @Param("maxDate") LocalDate maxDate
    );

    Optional<RuleEntityEntity> findByRuleSystemCodeAndRuleEntityTypeCodeAndCode(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code
    );

    Optional<RuleEntityEntity> findByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate startDate
    );

    @Query("""
        select (count(re) > 0)
        from RuleEntityEntity re
        where re.ruleSystemCode = :ruleSystemCode
          and re.ruleEntityTypeCode = :ruleEntityTypeCode
          and re.code = :code
          and re.startDate <> :excludedStartDate
          and re.startDate <= coalesce(:projectedEndDate, :maxDate)
          and :projectedStartDate <= coalesce(re.endDate, :maxDate)
        """)
    boolean existsOverlapExcludingStartDate(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("ruleEntityTypeCode") String ruleEntityTypeCode,
            @Param("code") String code,
            @Param("projectedStartDate") LocalDate projectedStartDate,
            @Param("projectedEndDate") LocalDate projectedEndDate,
            @Param("excludedStartDate") LocalDate excludedStartDate,
            @Param("maxDate") LocalDate maxDate
    );

    long deleteByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate startDate
    );
}
