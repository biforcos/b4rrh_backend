package com.b4rrhh.payroll_engine.table.infrastructure.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTableRowRequest(
        String searchCode,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyValue,
        BigDecimal annualValue,
        BigDecimal dailyValue,
        BigDecimal hourlyValue,
        Boolean active
) {}
