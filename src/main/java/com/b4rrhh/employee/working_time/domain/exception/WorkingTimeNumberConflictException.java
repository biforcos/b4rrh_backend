package com.b4rrhh.employee.working_time.domain.exception;

public class WorkingTimeNumberConflictException extends RuntimeException {

    public WorkingTimeNumberConflictException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer workingTimeNumber,
            Throwable cause
    ) {
        super("Working time number conflict for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", workingTimeNumber="
                + workingTimeNumber, cause);
    }
}