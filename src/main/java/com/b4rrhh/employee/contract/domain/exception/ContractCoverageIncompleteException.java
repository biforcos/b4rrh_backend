package com.b4rrhh.employee.contract.domain.exception;

public class ContractCoverageIncompleteException extends RuntimeException {

    public ContractCoverageIncompleteException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Contract coverage is incomplete for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}
