package com.b4rrhh.payroll.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCalculationRunRepository extends JpaRepository<CalculationRunEntity, Long> {
}