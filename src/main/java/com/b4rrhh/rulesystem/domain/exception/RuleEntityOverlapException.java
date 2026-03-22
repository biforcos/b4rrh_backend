package com.b4rrhh.rulesystem.domain.exception;

public class RuleEntityOverlapException extends RuntimeException {

    public RuleEntityOverlapException(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        super("Rule entity overlap detected for: " + ruleSystemCode + "/" + ruleEntityTypeCode + "/" + code);
    }
}
