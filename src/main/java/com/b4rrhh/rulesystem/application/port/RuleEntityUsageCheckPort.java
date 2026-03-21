package com.b4rrhh.rulesystem.application.port;

public interface RuleEntityUsageCheckPort {

    boolean isRuleEntityUsed(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code
    );
}
