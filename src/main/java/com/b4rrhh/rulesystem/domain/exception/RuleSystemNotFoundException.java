package com.b4rrhh.rulesystem.domain.exception;

public class RuleSystemNotFoundException extends RuntimeException {

    public RuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found: " + ruleSystemCode);
    }
}
