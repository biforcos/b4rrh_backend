package com.b4rrhh.employee.labor_classification.domain.exception;

import java.time.LocalDate;

public class LaborClassificationOutsidePresencePeriodException extends RuntimeException {

    public LaborClassificationOutsidePresencePeriodException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Labor classification period must be fully contained in employee presence history for ruleSystemCode="
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
