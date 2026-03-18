package com.b4rrhh.employee.cost_center.domain.exception;

import java.time.LocalDate;

public class CostCenterOutsidePresencePeriodException extends RuntimeException {

    public CostCenterOutsidePresencePeriodException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Cost center period must be fully contained in employee presence history for ruleSystemCode="
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
