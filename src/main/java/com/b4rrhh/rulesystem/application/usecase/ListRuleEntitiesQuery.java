package com.b4rrhh.rulesystem.application.usecase;

import java.time.LocalDate;

public record ListRuleEntitiesQuery(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        String code,
        Boolean active,
        LocalDate referenceDate
) {
}
