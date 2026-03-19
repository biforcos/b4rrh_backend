package com.b4rrhh.employee.labor_classification.application.command;

import java.time.LocalDate;

public record GetLaborClassificationByBusinessKeyCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate
) {
}
