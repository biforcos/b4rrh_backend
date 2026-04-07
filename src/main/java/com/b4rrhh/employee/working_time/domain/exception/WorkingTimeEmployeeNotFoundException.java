package com.b4rrhh.employee.working_time.domain.exception;

public class WorkingTimeEmployeeNotFoundException extends RuntimeException {

    public WorkingTimeEmployeeNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Employee not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}