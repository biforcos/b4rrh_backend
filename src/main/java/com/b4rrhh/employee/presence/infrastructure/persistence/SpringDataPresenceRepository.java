package com.b4rrhh.employee.presence.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataPresenceRepository extends JpaRepository<PresenceEntity, Long> {

        Optional<PresenceEntity> findByEmployeeIdAndPresenceNumber(Long employeeId, Integer presenceNumber);

    List<PresenceEntity> findByEmployeeIdOrderByStartDateAsc(Long employeeId);

    boolean existsByEmployeeIdAndEndDateIsNull(Long employeeId);

    @Query("""
            select max(p.presenceNumber)
            from PresenceEntity p
            where p.employeeId = :employeeId
            """)
    Integer findMaxPresenceNumberByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("""
            select case when count(p) > 0 then true else false end
            from PresenceEntity p
            where p.employeeId = :employeeId
              and p.startDate <= :effectiveEndDate
              and :startDate <= coalesce(p.endDate, :maxDate)
            """)
    boolean existsOverlappingPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("effectiveEndDate") LocalDate effectiveEndDate,
            @Param("maxDate") LocalDate maxDate
    );
}
