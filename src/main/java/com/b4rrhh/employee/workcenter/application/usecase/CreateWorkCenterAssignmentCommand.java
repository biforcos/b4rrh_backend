package com.b4rrhh.employee.workcenter.application.usecase;

import java.time.LocalDate;

public record CreateWorkCenterAssignmentCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String workCenterCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
