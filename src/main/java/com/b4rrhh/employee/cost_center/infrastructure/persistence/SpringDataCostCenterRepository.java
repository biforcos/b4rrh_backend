package com.b4rrhh.employee.cost_center.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataCostCenterRepository extends JpaRepository<CostCenterEntity, Long> {

    Optional<CostCenterEntity> findByEmployeeIdAndCostCenterCodeAndStartDate(
            Long employeeId,
            String costCenterCode,
            LocalDate startDate
    );

    List<CostCenterEntity> findByEmployeeIdOrderByStartDateAsc(Long employeeId);

    @Query("""
            select case when count(c) > 0 then true else false end
            from CostCenterEntity c
            where c.employeeId = :employeeId
              and c.costCenterCode = :costCenterCode
              and c.startDate <= :effectiveEndDate
              and :startDate <= coalesce(c.endDate, :maxDate)
            """)
    boolean existsOverlappingPeriodByCostCenterCode(
            @Param("employeeId") Long employeeId,
            @Param("costCenterCode") String costCenterCode,
            @Param("startDate") LocalDate startDate,
            @Param("effectiveEndDate") LocalDate effectiveEndDate,
            @Param("maxDate") LocalDate maxDate
    );
}
