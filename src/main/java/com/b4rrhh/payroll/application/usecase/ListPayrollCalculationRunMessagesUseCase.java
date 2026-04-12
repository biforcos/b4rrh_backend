package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.CalculationRunMessage;

import java.util.List;

public interface ListPayrollCalculationRunMessagesUseCase {

    List<CalculationRunMessage> listByRunId(Long runId);
}