package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.CalculationRun;

import java.util.Optional;

public interface GetPayrollCalculationRunUseCase {

    Optional<CalculationRun> getById(Long runId);
}