package com.b4rrhh.employee.employee.domain.exception;

public class EmployeeRuleSystemNotFoundException extends RuntimeException {

    public EmployeeRuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found with code: " + ruleSystemCode);
    }
}
