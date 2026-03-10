package com.b4rrhh.rulesystem.infrastructure.web.dto;

public record RuleSystemResponse(
        Long id,
        String code,
        String name,
        String countryCode,
        boolean active
) {
}