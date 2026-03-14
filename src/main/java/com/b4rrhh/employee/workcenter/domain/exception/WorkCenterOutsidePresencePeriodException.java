package com.b4rrhh.employee.workcenter.domain.exception;

import java.time.LocalDate;

public class WorkCenterOutsidePresencePeriodException extends RuntimeException {

    public WorkCenterOutsidePresencePeriodException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Work center period must be fully contained in employee presence history for ruleSystemCode="
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