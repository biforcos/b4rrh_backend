package com.b4rrhh.rulesystem.infrastructure.web.dto;

import java.time.LocalDate;

public record CorrectRuleEntityRequest(
        String name,
        String description,
        LocalDate endDate
) {
}
