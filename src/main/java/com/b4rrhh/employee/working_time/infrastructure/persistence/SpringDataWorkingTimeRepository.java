package com.b4rrhh.employee.working_time.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataWorkingTimeRepository extends JpaRepository<WorkingTimeEntity, Long> {

    Optional<WorkingTimeEntity> findByEmployeeIdAndWorkingTimeNumber(Long employeeId, Integer workingTimeNumber);

    List<WorkingTimeEntity> findByEmployeeIdOrderByStartDateAsc(Long employeeId);

    @Query("""
            select max(w.workingTimeNumber)
            from WorkingTimeEntity w
            where w.employeeId = :employeeId
            """)
    Integer findMaxWorkingTimeNumberByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("""
            select case when count(w) > 0 then true else false end
            from WorkingTimeEntity w
            where w.employeeId = :employeeId
              and w.startDate <= :effectiveEndDate
              and :startDate <= coalesce(w.endDate, :maxDate)
            """)
    boolean existsOverlappingPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("effectiveEndDate") LocalDate effectiveEndDate,
            @Param("maxDate") LocalDate maxDate
    );
}