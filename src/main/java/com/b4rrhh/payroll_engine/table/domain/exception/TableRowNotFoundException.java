package com.b4rrhh.payroll_engine.table.domain.exception;

public class TableRowNotFoundException extends RuntimeException {
    public TableRowNotFoundException(Long id) {
        super("Table row not found: id=" + id);
    }
}
