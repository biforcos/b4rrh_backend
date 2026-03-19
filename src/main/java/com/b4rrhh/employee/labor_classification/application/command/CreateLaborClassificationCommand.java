package com.b4rrhh.employee.labor_classification.application.command;

import java.time.LocalDate;

public record CreateLaborClassificationCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String agreementCode,
        String agreementCategoryCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
