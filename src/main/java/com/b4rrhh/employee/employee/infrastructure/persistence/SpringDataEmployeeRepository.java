package com.b4rrhh.employee.employee.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataEmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    Optional<EmployeeEntity> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    @Query("""
            select new com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeDirectoryProjection(
                e.ruleSystemCode,
                e.employeeTypeCode,
                e.employeeNumber,
                e.firstName,
                e.lastName1,
                e.lastName2,
                e.preferredName,
                e.status,
                (
                    select w.workCenterCode
                    from WorkCenterEntity w
                    where w.employeeId = e.id
                      and w.startDate <= :today
                      and (w.endDate is null or w.endDate >= :today)
                      and w.workCenterAssignmentNumber = (
                          select max(w2.workCenterAssignmentNumber)
                          from WorkCenterEntity w2
                          where w2.employeeId = e.id
                            and w2.startDate <= :today
                            and (w2.endDate is null or w2.endDate >= :today)
                      )
                )
            )
            from EmployeeEntity e
            where (:ruleSystemCode is null or e.ruleSystemCode = :ruleSystemCode)
              and (:employeeTypeCode is null or e.employeeTypeCode = :employeeTypeCode)
              and (:status is null or e.status = :status)
              and (
                    :queryText is null
                    or upper(e.employeeNumber) like concat('%', :queryText, '%')
                    or upper(e.ruleSystemCode) like concat('%', :queryText, '%')
                    or upper(e.employeeTypeCode) like concat('%', :queryText, '%')
                    or upper(e.firstName) like concat('%', :queryText, '%')
                    or upper(e.lastName1) like concat('%', :queryText, '%')
                    or upper(coalesce(e.lastName2, '')) like concat('%', :queryText, '%')
                    or upper(coalesce(e.preferredName, '')) like concat('%', :queryText, '%')
                    or upper(
                        concat(
                            concat(concat(e.firstName, ' '), e.lastName1),
                            concat(' ', coalesce(e.lastName2, ''))
                        )
                    ) like concat('%', :queryText, '%')
              )
            order by e.ruleSystemCode, e.employeeTypeCode, e.employeeNumber
            """)
    List<EmployeeDirectoryProjection> findDirectoryByFilters(
            @Param("queryText") String queryText,
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("employeeTypeCode") String employeeTypeCode,
            @Param("status") String status,
            @Param("today") LocalDate today,
            Pageable pageable
    );
}