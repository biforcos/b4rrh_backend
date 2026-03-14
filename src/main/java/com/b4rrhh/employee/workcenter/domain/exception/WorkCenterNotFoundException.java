package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterNotFoundException extends RuntimeException {

    public WorkCenterNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer workCenterAssignmentNumber
    ) {
        super("Work center assignment not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", workCenterAssignmentNumber="
                + workCenterAssignmentNumber);
    }
}