package com.b4rrhh.employee.working_time.infrastructure.persistence;

import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.labor_classification.infrastructure.persistence.LaborClassificationEntity;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeAgreementContextRepository extends JpaRepository<LaborClassificationEntity, Long> {

    @Query("""
            select new com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext(
                e.ruleSystemCode,
                l.agreementCode
            )
            from LaborClassificationEntity l, EmployeeEntity e
            where l.employeeId = e.id
              and l.employeeId = :employeeId
              and l.startDate <= :effectiveDate
              and (l.endDate is null or l.endDate >= :effectiveDate)
            order by l.startDate desc
            """)
    List<EmployeeAgreementContext> findLatestValidByEmployeeIdAndEffectiveDate(
            @Param("employeeId") Long employeeId,
            @Param("effectiveDate") LocalDate effectiveDate,
            Pageable pageable
    );
}
