package com.b4rrhh.employee.labor_classification.domain.exception;

public class LaborClassificationCoverageIncompleteException extends RuntimeException {

    public LaborClassificationCoverageIncompleteException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Labor classification coverage is incomplete for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
