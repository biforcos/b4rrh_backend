package com.b4rrhh.payroll_engine.concept.infrastructure.persistence;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataPayrollConceptFeedRelationRepository
        extends JpaRepository<PayrollConceptFeedRelationEntity, Long> {

    @Query("""
        select r from PayrollEngineFeedRelationEntity r
        where r.targetObject.id = :targetObjectId
          and r.effectiveFrom <= :referenceDate
          and (r.effectiveTo is null or r.effectiveTo >= :referenceDate)
        """)
    List<PayrollConceptFeedRelationEntity> findActiveByTargetObjectId(
            @Param("targetObjectId") Long targetObjectId,
            @Param("referenceDate") LocalDate referenceDate
    );

    @Query("""
        select r from PayrollEngineFeedRelationEntity r
        where r.targetObject.ruleSystemCode = :ruleSystemCode
          and r.targetObject.objectCode = :conceptCode
        order by r.sourceObject.objectCode asc
        """)
    List<PayrollConceptFeedRelationEntity> findByRuleSystemCodeAndTargetConceptCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCode") String conceptCode
    );

    @Modifying
    @Transactional
    @Query("""
        delete from PayrollEngineFeedRelationEntity r
        where r.targetObject.ruleSystemCode = :ruleSystemCode
          and r.targetObject.objectCode = :conceptCode
        """)
    void deleteAllByRuleSystemCodeAndTargetConceptCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("conceptCode") String conceptCode
    );
}
