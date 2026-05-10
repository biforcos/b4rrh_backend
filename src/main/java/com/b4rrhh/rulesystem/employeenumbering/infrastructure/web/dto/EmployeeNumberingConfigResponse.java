package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto;

public record EmployeeNumberingConfigResponse(
        String ruleSystemCode,
        String prefix,
        int numericPartLength,
        int step,
        long nextValue,
        String nextNumberPreview
) {}
