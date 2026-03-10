package com.b4rrhh.rulesystem.application.usecase;

public record CreateRuleEntityTypeCommand(
        String code,
        String name
) {
}
