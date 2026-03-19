package com.b4rrhh.employee.contract.domain.exception;

import java.time.LocalDate;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate
    ) {
        super("Contract not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", startDate="
                + startDate);
    }
}
