package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;
import com.b4rrhh.payroll.domain.model.CalculationRun;
import com.b4rrhh.payroll.domain.port.CalculationRunRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetPayrollCalculationRunService implements GetPayrollCalculationRunUseCase {

    private final CalculationRunRepository calculationRunRepository;

    public GetPayrollCalculationRunService(CalculationRunRepository calculationRunRepository) {
        this.calculationRunRepository = calculationRunRepository;
    }

    @Override
    public Optional<CalculationRun> getById(Long runId) {
        if (runId == null || runId <= 0) {
            throw new InvalidPayrollArgumentException("runId must be a positive integer");
        }
        return calculationRunRepository.findById(runId);
    }
}