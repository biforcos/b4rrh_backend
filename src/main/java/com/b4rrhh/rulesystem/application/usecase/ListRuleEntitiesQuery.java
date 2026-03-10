package com.b4rrhh.rulesystem.application.usecase;

public record ListRuleEntitiesQuery(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        String code,
        Boolean active
) {
}
