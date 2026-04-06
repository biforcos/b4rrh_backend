package com.b4rrhh.rulesystem.workcenter.domain.exception;

public class WorkCenterContactAlreadyExistsException extends RuntimeException {

    public WorkCenterContactAlreadyExistsException(String ruleSystemCode, String workCenterCode, Integer contactNumber) {
        super("Work center contact already exists for business key: "
                + ruleSystemCode + "/" + workCenterCode + "/" + contactNumber);
    }
}