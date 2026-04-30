package com.b4rrhh.payroll_engine.table.application.usecase;

import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;

public interface CreateTableRowUseCase {
    PayrollTableRow create(CreateTableRowCommand command);
}
