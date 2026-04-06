package com.b4rrhh.rulesystem.workcenter.domain.exception;

public class WorkCenterContactTypeInvalidException extends RuntimeException {

    public WorkCenterContactTypeInvalidException(String ruleSystemCode, String contactTypeCode) {
        super("Invalid CONTACT_TYPE " + contactTypeCode + " for rule system " + ruleSystemCode);
    }
}