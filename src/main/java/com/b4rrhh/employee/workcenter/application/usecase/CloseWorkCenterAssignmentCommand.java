package com.b4rrhh.employee.workcenter.application.usecase;

import java.time.LocalDate;

public record CloseWorkCenterAssignmentCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer workCenterAssignmentNumber,
        LocalDate endDate
) {
}
