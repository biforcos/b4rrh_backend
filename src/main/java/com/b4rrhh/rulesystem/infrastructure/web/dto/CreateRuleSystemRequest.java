package com.b4rrhh.rulesystem.infrastructure.web.dto;

public record CreateRuleSystemRequest(
        String code,
        String name,
        String countryCode
) {
}