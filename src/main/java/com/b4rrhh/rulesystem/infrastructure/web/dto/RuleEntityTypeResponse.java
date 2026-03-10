package com.b4rrhh.rulesystem.infrastructure.web.dto;

public record RuleEntityTypeResponse(
        Long id,
        String code,
        String name,
        boolean active
) {
}
