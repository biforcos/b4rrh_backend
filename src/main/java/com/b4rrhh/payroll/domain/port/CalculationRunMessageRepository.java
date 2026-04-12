package com.b4rrhh.payroll.domain.port;

import com.b4rrhh.payroll.domain.model.CalculationRunMessage;

import java.util.List;

public interface CalculationRunMessageRepository {

    CalculationRunMessage save(CalculationRunMessage calculationRunMessage);

    List<CalculationRunMessage> findByRunIdOrderByCreatedAtAscIdAsc(Long runId);
}