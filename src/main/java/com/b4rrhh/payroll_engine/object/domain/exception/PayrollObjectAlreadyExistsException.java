package com.b4rrhh.payroll_engine.object.domain.exception;

public class PayrollObjectAlreadyExistsException extends RuntimeException {

    public PayrollObjectAlreadyExistsException(String ruleSystemCode, String objectTypeCode, String objectCode) {
        super("PayrollObject already exists: ruleSystemCode=" + ruleSystemCode
                + ", objectTypeCode=" + objectTypeCode
                + ", objectCode=" + objectCode);
    }
}
