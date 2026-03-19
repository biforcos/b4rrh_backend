package com.b4rrhh.employee.contract.domain.exception;

import java.time.LocalDate;

public class ContractOutsidePresencePeriodException extends RuntimeException {

    public ContractOutsidePresencePeriodException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Contract period must be fully contained in employee presence history for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", periodStart="
                + startDate
                + ", periodEnd="
                + endDate);
    }
}
