package com.b4rrhh.employee.presence.domain.exception;

public class PresenceRuleSystemNotFoundException extends RuntimeException {

    public PresenceRuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found with code: " + ruleSystemCode);
    }
}
