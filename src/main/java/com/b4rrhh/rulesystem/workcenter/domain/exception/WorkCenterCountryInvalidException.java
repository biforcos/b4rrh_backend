package com.b4rrhh.rulesystem.workcenter.domain.exception;

public class WorkCenterCountryInvalidException extends RuntimeException {

    public WorkCenterCountryInvalidException(String ruleSystemCode, String countryCode) {
        super("Invalid COUNTRY " + countryCode + " for rule system " + ruleSystemCode);
    }
}