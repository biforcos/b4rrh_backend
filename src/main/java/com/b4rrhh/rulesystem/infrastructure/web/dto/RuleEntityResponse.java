package com.b4rrhh.rulesystem.infrastructure.web.dto;

import java.time.LocalDate;

public record RuleEntityResponse(
        Long id,
        String ruleSystemCode,
        String ruleEntityTypeCode,
        String code,
        String name,
        String description,
        boolean active,
        LocalDate startDate,
        LocalDate endDate
) {
}
