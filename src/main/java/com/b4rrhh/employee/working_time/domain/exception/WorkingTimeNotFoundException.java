package com.b4rrhh.employee.working_time.domain.exception;

public class WorkingTimeNotFoundException extends RuntimeException {

    public WorkingTimeNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer workingTimeNumber
    ) {
        super("Working time not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", workingTimeNumber="
                + workingTimeNumber);
    }
}