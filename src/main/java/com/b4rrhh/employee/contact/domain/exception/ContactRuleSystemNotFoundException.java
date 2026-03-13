package com.b4rrhh.employee.contact.domain.exception;

public class ContactRuleSystemNotFoundException extends RuntimeException {

    public ContactRuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found with code: " + ruleSystemCode);
    }
}
