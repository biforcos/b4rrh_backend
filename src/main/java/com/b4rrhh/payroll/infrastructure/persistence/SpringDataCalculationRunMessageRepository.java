package com.b4rrhh.payroll.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCalculationRunMessageRepository extends JpaRepository<CalculationRunMessageEntity, Long> {

	List<CalculationRunMessageEntity> findByCalculationRunIdOrderByCreatedAtAscIdAsc(Long runId);
}