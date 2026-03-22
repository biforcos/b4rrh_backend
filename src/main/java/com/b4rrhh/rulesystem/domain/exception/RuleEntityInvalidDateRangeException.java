package com.b4rrhh.rulesystem.domain.exception;

import java.time.LocalDate;

public class RuleEntityInvalidDateRangeException extends RuntimeException {

    public RuleEntityInvalidDateRangeException(LocalDate startDate, LocalDate endDate) {
        super("Invalid rule entity date range: startDate=" + startDate + ", endDate=" + endDate);
    }
}
