package com.b4rrhh.employee.cost_center.application.command;

import java.time.LocalDate;

public record GetCostCenterByBusinessKeyCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String costCenterCode,
        LocalDate startDate
) {
}
