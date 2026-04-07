package com.b4rrhh.employee.working_time.domain.exception;

import java.time.LocalDate;

public class WorkingTimeOverlapException extends RuntimeException {

    public WorkingTimeOverlapException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Working time period overlaps for ruleSystemCode="
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