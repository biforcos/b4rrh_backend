package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterEmployeeNotFoundException extends RuntimeException {

    public WorkCenterEmployeeNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Employee not found with business key ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}