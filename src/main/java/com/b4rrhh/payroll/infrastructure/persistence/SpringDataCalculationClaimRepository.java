package com.b4rrhh.payroll.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCalculationClaimRepository extends JpaRepository<CalculationClaimEntity, Long> {

	void deleteByCalculationRunId(Long runId);
}