package com.b4rrhh.rulesystem.workcenter.domain.exception;

public class WorkCenterAlreadyExistsException extends RuntimeException {

    public WorkCenterAlreadyExistsException(String ruleSystemCode, String workCenterCode) {
        super("Work center already exists for business key: " + ruleSystemCode + "/" + workCenterCode);
    }
}