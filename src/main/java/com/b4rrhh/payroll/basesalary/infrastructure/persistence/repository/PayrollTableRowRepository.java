package com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository;

import com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity.PayrollTableRowEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for PayrollTableRowEntity.
 * Persistence port.
 */
public interface PayrollTableRowRepository extends JpaRepository<PayrollTableRowEntity, Long> {

    /**
     * Find the active table row valid for the given date.
     * Returns the most recent row where start_date <= effectiveDate and (end_date is null or end_date >= effectiveDate).
     */
    @Query("""
        select r from PayrollTableRowEntity r
        where r.ruleSystemCode = :ruleSystemCode
          and r.tableCode = :tableCode
          and r.searchCode = :searchCode
          and r.active = true
          and r.startDate <= :effectiveDate
          and (r.endDate is null or r.endDate >= :effectiveDate)
        order by r.startDate desc
        """)
    List<PayrollTableRowEntity> findLatestValidByRuleSystemCodeAndTableCodeAndSearchCodeAndEffectiveDate(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("tableCode") String tableCode,
            @Param("searchCode") String searchCode,
            @Param("effectiveDate") LocalDate effectiveDate,
            Pageable pageable
    );
}
