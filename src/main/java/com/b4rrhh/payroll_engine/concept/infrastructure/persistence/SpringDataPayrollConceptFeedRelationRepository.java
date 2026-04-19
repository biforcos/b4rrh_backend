package com.b4rrhh.payroll_engine.concept.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
