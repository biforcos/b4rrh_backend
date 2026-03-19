package com.b4rrhh.employee.labor_classification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataLaborClassificationRepository extends JpaRepository<LaborClassificationEntity, Long> {

    Optional<LaborClassificationEntity> findByEmployeeIdAndStartDate(Long employeeId, LocalDate startDate);

    List<LaborClassificationEntity> findByEmployeeIdOrderByStartDateAsc(Long employeeId);

    @Query("""
            select case when count(l) > 0 then true else false end
            from LaborClassificationEntity l
            where l.employeeId = :employeeId
              and l.startDate <= :effectiveEndDate
              and :startDate <= coalesce(l.endDate, :maxDate)
              and (:excludeStartDate is null or l.startDate <> :excludeStartDate)
            """)
    boolean existsOverlappingPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("effectiveEndDate") LocalDate effectiveEndDate,
            @Param("maxDate") LocalDate maxDate,
            @Param("excludeStartDate") LocalDate excludeStartDate
    );
}
