package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.domain.model.PayrollStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataPayrollRepository extends JpaRepository<PayrollEntity, Long> {

    @EntityGraph(attributePaths = {"concepts", "contextSnapshots", "warnings", "segments"})
    Optional<PayrollEntity> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndPayrollPeriodCodeAndPayrollTypeCodeAndPresenceNumber(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String payrollPeriodCode,
            String payrollTypeCode,
            Integer presenceNumber
    );

    @Query("SELECT p FROM PayrollEntity p WHERE " +
           "(:ruleSystemCode IS NULL OR p.ruleSystemCode = :ruleSystemCode) AND " +
           "(:payrollPeriodCode IS NULL OR p.payrollPeriodCode = :payrollPeriodCode) AND " +
           "(:employeeNumber IS NULL OR p.employeeNumber = :employeeNumber) AND " +
           "(:status IS NULL OR p.status = :status) " +
           "ORDER BY p.calculatedAt DESC")
    List<PayrollEntity> findByFilters(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("payrollPeriodCode") String payrollPeriodCode,
            @Param("employeeNumber") String employeeNumber,
            @Param("status") PayrollStatus status,
            Pageable pageable
    );
}
