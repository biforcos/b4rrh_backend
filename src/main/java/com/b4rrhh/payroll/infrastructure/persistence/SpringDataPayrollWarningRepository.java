package com.b4rrhh.payroll.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPayrollWarningRepository extends JpaRepository<PayrollWarningEntity, Long> {
}