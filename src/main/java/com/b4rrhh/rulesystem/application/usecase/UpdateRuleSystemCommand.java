package com.b4rrhh.rulesystem.application.usecase;

public record UpdateRuleSystemCommand(
        String ruleSystemCode,
        String name,
        String countryCode,
        Boolean active
) {
}
