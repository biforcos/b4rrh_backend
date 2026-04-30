package com.b4rrhh.payroll_engine.table.domain.exception;

import java.time.LocalDate;

public class TableRowAlreadyExistsException extends RuntimeException {
    public TableRowAlreadyExistsException(String tableCode, String searchCode, LocalDate startDate) {
        super("Table row already exists: tableCode=" + tableCode
                + ", searchCode=" + searchCode + ", startDate=" + startDate);
    }
}
