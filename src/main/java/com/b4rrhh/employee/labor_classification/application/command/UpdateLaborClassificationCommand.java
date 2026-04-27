package com.b4rrhh.employee.labor_classification.application.command;

import java.time.LocalDate;

public record UpdateLaborClassificationCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate,
        LocalDate newStartDate,
        String agreementCode,
        String agreementCategoryCode
) {
}
