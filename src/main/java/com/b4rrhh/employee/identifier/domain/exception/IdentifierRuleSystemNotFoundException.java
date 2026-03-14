package com.b4rrhh.employee.identifier.domain.exception;

public class IdentifierRuleSystemNotFoundException extends RuntimeException {

    public IdentifierRuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found with code: " + ruleSystemCode);
    }
}
