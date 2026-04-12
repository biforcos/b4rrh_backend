package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.CalculationRun;

public interface LaunchPayrollCalculationUseCase {

    CalculationRun launch(LaunchPayrollCalculationCommand command);
}