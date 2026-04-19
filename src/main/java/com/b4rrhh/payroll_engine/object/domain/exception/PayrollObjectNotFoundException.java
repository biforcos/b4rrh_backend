package com.b4rrhh.payroll_engine.object.domain.exception;

public class PayrollObjectNotFoundException extends RuntimeException {

    public PayrollObjectNotFoundException(String ruleSystemCode, String objectTypeCode, String objectCode) {
        super("PayrollObject not found: ruleSystemCode=" + ruleSystemCode
                + ", objectTypeCode=" + objectTypeCode
                + ", objectCode=" + objectCode);
    }
}
