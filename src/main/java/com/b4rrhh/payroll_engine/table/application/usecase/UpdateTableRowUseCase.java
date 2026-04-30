package com.b4rrhh.payroll_engine.table.application.usecase;

import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;

public interface UpdateTableRowUseCase {
    PayrollTableRow update(UpdateTableRowCommand command);
}
