package com.b4rrhh.payroll_engine.table.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTableRowCommand(
        Long id,
        String searchCode,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyValue,
        BigDecimal annualValue,
        BigDecimal dailyValue,
        BigDecimal hourlyValue,
        Boolean active
) {}
