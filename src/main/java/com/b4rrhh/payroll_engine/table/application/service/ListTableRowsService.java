package com.b4rrhh.payroll_engine.table.application.service;

import com.b4rrhh.payroll_engine.table.application.usecase.ListTableRowsUseCase;
import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;
import com.b4rrhh.payroll_engine.table.domain.port.PayrollTableRowManagementPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListTableRowsService implements ListTableRowsUseCase {

    private final PayrollTableRowManagementPort port;

    public ListTableRowsService(PayrollTableRowManagementPort port) {
        this.port = port;
    }

    @Override
    public List<PayrollTableRow> list(String ruleSystemCode, String tableCode) {
        return port.findAllByTableCode(ruleSystemCode, tableCode);
    }
}
