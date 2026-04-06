package com.b4rrhh.rulesystem.workcenter.domain.exception;

public class WorkCenterContactNotFoundException extends RuntimeException {

    public WorkCenterContactNotFoundException(String ruleSystemCode, String workCenterCode, Integer contactNumber) {
        super("Work center contact not found for business key: "
                + ruleSystemCode + "/" + workCenterCode + "/" + contactNumber);
    }
}