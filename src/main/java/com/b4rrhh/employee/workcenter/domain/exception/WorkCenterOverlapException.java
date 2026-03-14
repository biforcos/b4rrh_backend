package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterOverlapException extends RuntimeException {

    public WorkCenterOverlapException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Work center period overlaps for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}