package com.b4rrhh.payroll_engine.table.application.service;

import com.b4rrhh.payroll_engine.table.application.usecase.DeleteTableRowUseCase;
import com.b4rrhh.payroll_engine.table.domain.exception.TableRowNotFoundException;
import com.b4rrhh.payroll_engine.table.domain.port.PayrollTableRowManagementPort;
import org.springframework.stereotype.Service;

@Service
public class DeleteTableRowService implements DeleteTableRowUseCase {

    private final PayrollTableRowManagementPort port;

    public DeleteTableRowService(PayrollTableRowManagementPort port) {
        this.port = port;
    }

    @Override
    public void delete(Long id) {
        port.findById(id).orElseThrow(() -> new TableRowNotFoundException(id));
        port.deleteById(id);
    }
}
