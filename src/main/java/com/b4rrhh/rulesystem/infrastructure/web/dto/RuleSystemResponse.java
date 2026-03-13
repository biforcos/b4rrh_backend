package com.b4rrhh.rulesystem.infrastructure.web.dto;

public record RuleSystemResponse(
        String code,
        String name,
        String countryCode,
        boolean active
) {
}