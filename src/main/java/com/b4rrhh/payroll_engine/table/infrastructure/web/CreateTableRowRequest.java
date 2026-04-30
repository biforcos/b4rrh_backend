package com.b4rrhh.payroll_engine.table.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTableRowRequest(
        @NotBlank String searchCode,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull BigDecimal monthlyValue,
        @NotNull BigDecimal annualValue,
        @NotNull BigDecimal dailyValue,
        @NotNull BigDecimal hourlyValue
) {}
