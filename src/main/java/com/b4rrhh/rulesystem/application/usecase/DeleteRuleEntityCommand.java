package com.b4rrhh.rulesystem.application.usecase;

import java.time.LocalDate;

public record DeleteRuleEntityCommand(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        String code,
        LocalDate startDate
) {
}
