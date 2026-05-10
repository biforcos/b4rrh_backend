package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto;

public record UpsertEmployeeNumberingConfigRequest(
        String prefix,
        int numericPartLength,
        int step,
        long nextValue
) {}
