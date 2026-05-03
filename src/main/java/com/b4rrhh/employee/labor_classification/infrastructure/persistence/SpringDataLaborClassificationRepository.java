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

    @Query(value = """
            select case when count(*) > 0 then true else false end
            from employee.labor_classification l
            where l.employee_id = :employeeId
              and l.start_date <= :effectiveEndDate
              and :startDate <= coalesce(l.end_date, :maxDate)
              and (cast(:excludeStartDate as date) is null or l.start_date <> :excludeStartDate)
            """, nativeQuery = true)
    boolean existsOverlappingPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("effectiveEndDate") LocalDate effectiveEndDate,
            @Param("maxDate") LocalDate maxDate,
            @Param("excludeStartDate") LocalDate excludeStartDate
    );
}
