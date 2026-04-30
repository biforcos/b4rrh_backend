package com.b4rrhh.payroll_engine.table.application.usecase;

import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;
import java.util.List;

public interface ListTableRowsUseCase {
    List<PayrollTableRow> list(String ruleSystemCode, String tableCode);
}
