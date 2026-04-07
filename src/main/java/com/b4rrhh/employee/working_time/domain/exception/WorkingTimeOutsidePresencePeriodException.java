package com.b4rrhh.employee.working_time.domain.exception;

import java.time.LocalDate;

public class WorkingTimeOutsidePresencePeriodException extends RuntimeException {

    public WorkingTimeOutsidePresencePeriodException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Working time period is outside employee presence history for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", startDate="
                + startDate
                + ", endDate="
                + endDate);
    }
}