package com.b4rrhh.rulesystem.domain.exception;

public class RuleEntityInUseException extends RuntimeException {

    public RuleEntityInUseException(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        super("Rule entity is in use: " + ruleSystemCode + "/" + ruleEntityTypeCode + "/" + code);
    }
}
