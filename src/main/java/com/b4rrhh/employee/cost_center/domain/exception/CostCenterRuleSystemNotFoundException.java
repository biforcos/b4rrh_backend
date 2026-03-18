package com.b4rrhh.employee.cost_center.domain.exception;

public class CostCenterRuleSystemNotFoundException extends RuntimeException {

    public CostCenterRuleSystemNotFoundException(String ruleSystemCode) {
        super("Rule system not found for code=" + ruleSystemCode);
    }
}
