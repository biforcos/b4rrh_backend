package com.b4rrhh.payroll.domain.port;

import com.b4rrhh.payroll.domain.model.CalculationRunMessage;

public interface CalculationRunMessageRepository {

    CalculationRunMessage save(CalculationRunMessage calculationRunMessage);
}