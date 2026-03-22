package com.b4rrhh.employee.workcenter.application.usecase;

import java.time.LocalDate;

public record UpdateWorkCenterCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer workCenterAssignmentNumber,
        String workCenterCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
