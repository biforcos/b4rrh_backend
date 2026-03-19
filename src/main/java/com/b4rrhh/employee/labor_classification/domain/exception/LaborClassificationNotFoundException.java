package com.b4rrhh.employee.labor_classification.domain.exception;

import java.time.LocalDate;

public class LaborClassificationNotFoundException extends RuntimeException {

    public LaborClassificationNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate
    ) {
        super("Labor classification not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", startDate="
                + startDate);
    }
}
