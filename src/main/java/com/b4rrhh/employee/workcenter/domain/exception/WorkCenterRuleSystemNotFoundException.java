package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterRuleSystemNotFoundException extends RuntimeException {

    public WorkCenterRuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found with code: " + ruleSystemCode);
    }
}