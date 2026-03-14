package com.b4rrhh.employee.workcenter.application.usecase;

import java.time.LocalDate;

public record CreateWorkCenterCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String workCenterCode,
        LocalDate startDate,
        LocalDate endDate
) {
}