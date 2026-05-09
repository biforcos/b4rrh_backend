package com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase;

public record UpsertEmployeeDisplayNameFormatCommand(
        String ruleSystemCode,
        String formatCode
) {}
