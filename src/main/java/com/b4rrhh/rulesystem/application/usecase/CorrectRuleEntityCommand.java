package com.b4rrhh.rulesystem.application.usecase;

import java.time.LocalDate;

public record CorrectRuleEntityCommand(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        String code,
        LocalDate startDate,
        String name,
        String description,
        LocalDate endDate
) {
}
