package com.b4rrhh.rulesystem.workcenter.domain.exception;

public class WorkCenterCompanyInvalidException extends RuntimeException {

    public WorkCenterCompanyInvalidException(String ruleSystemCode, String companyCode) {
        super("Invalid COMPANY " + companyCode + " for rule system " + ruleSystemCode);
    }
}