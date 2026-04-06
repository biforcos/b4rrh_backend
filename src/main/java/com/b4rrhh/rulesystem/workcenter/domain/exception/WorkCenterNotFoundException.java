package com.b4rrhh.rulesystem.workcenter.domain.exception;

public class WorkCenterNotFoundException extends RuntimeException {

    public WorkCenterNotFoundException(String ruleSystemCode, String workCenterCode) {
        super("Work center not found for business key: " + ruleSystemCode + "/" + workCenterCode);
    }
}