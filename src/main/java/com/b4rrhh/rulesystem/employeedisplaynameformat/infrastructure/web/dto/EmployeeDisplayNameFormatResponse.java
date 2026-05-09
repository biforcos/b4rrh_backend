package com.b4rrhh.rulesystem.employeedisplaynameformat.infrastructure.web.dto;

public record EmployeeDisplayNameFormatResponse(
        String ruleSystemCode,
        String formatCode,
        String formatLabel,
        String example
) {}
