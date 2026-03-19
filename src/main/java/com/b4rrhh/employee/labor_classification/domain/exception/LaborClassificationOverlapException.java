package com.b4rrhh.employee.labor_classification.domain.exception;

import java.time.LocalDate;

public class LaborClassificationOverlapException extends RuntimeException {

    public LaborClassificationOverlapException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Labor classification period overlaps for ruleSystemCode="
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
