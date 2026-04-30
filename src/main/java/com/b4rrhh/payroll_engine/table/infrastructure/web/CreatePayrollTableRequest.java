package com.b4rrhh.payroll_engine.table.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record CreatePayrollTableRequest(@NotBlank String objectCode) {}
