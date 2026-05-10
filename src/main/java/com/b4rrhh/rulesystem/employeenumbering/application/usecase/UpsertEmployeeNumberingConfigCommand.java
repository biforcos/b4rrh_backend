package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

public record UpsertEmployeeNumberingConfigCommand(
        String ruleSystemCode,
        String prefix,
        int numericPartLength,
        int step,
        long nextValue
) {}
