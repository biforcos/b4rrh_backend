package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterPresenceCoverageGapException extends RuntimeException {

    public WorkCenterPresenceCoverageGapException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        super("Work center history must fully cover employee presence history for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber);
    }
}