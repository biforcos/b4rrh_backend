package com.b4rrhh.rulesystem.application.usecase;

public record CreateRuleSystemCommand(
        String code,
        String name,
        String countryCode
) {
}