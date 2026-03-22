package com.b4rrhh.rulesystem.domain.exception;

public class RuleEntityAlreadyClosedException extends RuntimeException {

    public RuleEntityAlreadyClosedException(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        super("Rule entity is already closed: " + ruleSystemCode + "/" + ruleEntityTypeCode + "/" + code);
    }
}
