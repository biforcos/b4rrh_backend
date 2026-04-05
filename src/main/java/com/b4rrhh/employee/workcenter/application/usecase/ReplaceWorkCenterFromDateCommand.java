package com.b4rrhh.employee.workcenter.application.usecase;

import java.time.LocalDate;

public record ReplaceWorkCenterFromDateCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate effectiveDate,
        String workCenterCode
) {
}