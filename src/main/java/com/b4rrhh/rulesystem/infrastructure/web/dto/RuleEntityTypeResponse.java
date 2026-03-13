package com.b4rrhh.rulesystem.infrastructure.web.dto;

public record RuleEntityTypeResponse(
        String code,
        String name,
        boolean active
) {
}
