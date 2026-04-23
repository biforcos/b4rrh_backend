package com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository;

import com.b4rrhh.employee.labor_classification.infrastructure.persistence.LaborClassificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeAgreementCategoryRepository extends JpaRepository<LaborClassificationEntity, Long> {

    @Query("""
            select l.agreementCategoryCode
            from LaborClassificationEntity l
            where l.employeeId = :employeeId
              and l.startDate <= :effectiveDate
              and (l.endDate is null or l.endDate >= :effectiveDate)
            order by l.startDate desc
            """)
        List<String> findLatestValidByEmployeeIdAndEffectiveDate(
            @Param("employeeId") Long employeeId,
            @Param("effectiveDate") LocalDate effectiveDate,
            Pageable pageable
    );
}
