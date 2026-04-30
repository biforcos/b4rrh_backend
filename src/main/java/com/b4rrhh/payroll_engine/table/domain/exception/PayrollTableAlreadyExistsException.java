package com.b4rrhh.payroll_engine.table.domain.exception;

public class PayrollTableAlreadyExistsException extends RuntimeException {
    public PayrollTableAlreadyExistsException(String ruleSystemCode, String objectCode) {
        super("Salary table already exists: ruleSystemCode=" + ruleSystemCode + ", objectCode=" + objectCode);
    }
}
