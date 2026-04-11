package com.b4rrhh.payroll.domain.port;

import com.b4rrhh.payroll.domain.model.CalculationRun;

import java.util.Optional;

public interface CalculationRunRepository {

    CalculationRun save(CalculationRun calculationRun);

    Optional<CalculationRun> findById(Long id);
}