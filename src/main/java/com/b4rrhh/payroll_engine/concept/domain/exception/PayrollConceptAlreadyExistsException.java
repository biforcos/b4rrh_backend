package com.b4rrhh.payroll_engine.concept.domain.exception;

public class PayrollConceptAlreadyExistsException extends RuntimeException {

    public PayrollConceptAlreadyExistsException(String ruleSystemCode, String conceptCode) {
        super("PayrollConcept already exists: ruleSystemCode=" + ruleSystemCode
                + ", conceptCode=" + conceptCode);
    }
}
