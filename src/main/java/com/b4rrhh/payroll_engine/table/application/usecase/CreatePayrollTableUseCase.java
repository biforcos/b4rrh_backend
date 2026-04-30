package com.b4rrhh.payroll_engine.table.application.usecase;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;

public interface CreatePayrollTableUseCase {
    PayrollObject create(CreatePayrollTableCommand command);
}
