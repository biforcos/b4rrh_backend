package com.b4rrhh.rulesystem.domain.exception;

import java.time.LocalDate;

public class RuleEntityNotFoundException extends RuntimeException {

    public RuleEntityNotFoundException(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            LocalDate startDate
    ) {
        super("Rule entity not found: " + ruleSystemCode + "/" + ruleEntityTypeCode + "/" + code + "/" + startDate);
    }
}
