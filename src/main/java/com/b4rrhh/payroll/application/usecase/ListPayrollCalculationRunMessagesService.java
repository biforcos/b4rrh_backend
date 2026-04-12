package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;
import com.b4rrhh.payroll.domain.model.CalculationRunMessage;
import com.b4rrhh.payroll.domain.port.CalculationRunMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListPayrollCalculationRunMessagesService implements ListPayrollCalculationRunMessagesUseCase {

    private final CalculationRunMessageRepository calculationRunMessageRepository;

    public ListPayrollCalculationRunMessagesService(CalculationRunMessageRepository calculationRunMessageRepository) {
        this.calculationRunMessageRepository = calculationRunMessageRepository;
    }

    @Override
    public List<CalculationRunMessage> listByRunId(Long runId) {
        if (runId == null || runId <= 0) {
            throw new InvalidPayrollArgumentException("runId must be a positive integer");
        }
        return calculationRunMessageRepository.findByRunIdOrderByCreatedAtAscIdAsc(runId);
    }
}