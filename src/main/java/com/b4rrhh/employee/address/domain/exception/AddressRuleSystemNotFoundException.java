package com.b4rrhh.employee.address.domain.exception;

public class AddressRuleSystemNotFoundException extends RuntimeException {

    public AddressRuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found with code: " + ruleSystemCode);
    }
}
