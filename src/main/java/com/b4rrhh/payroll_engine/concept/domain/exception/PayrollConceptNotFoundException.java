package com.b4rrhh.payroll_engine.concept.domain.exception;

public class PayrollConceptNotFoundException extends RuntimeException {

    public PayrollConceptNotFoundException(String ruleSystemCode, String conceptCode) {
        super("PayrollConcept not found: ruleSystemCode=" + ruleSystemCode
                + ", conceptCode=" + conceptCode);
    }
}
