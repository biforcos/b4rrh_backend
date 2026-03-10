package com.b4rrhh.rulesystem.application.usecase;

import java.time.LocalDate;

public record CreateRuleEntityCommand(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        String code,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
