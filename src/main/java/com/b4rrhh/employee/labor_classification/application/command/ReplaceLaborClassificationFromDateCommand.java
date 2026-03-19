package com.b4rrhh.employee.labor_classification.application.command;

import java.time.LocalDate;

public record ReplaceLaborClassificationFromDateCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate effectiveDate,
        String agreementCode,
        String agreementCategoryCode
) {
}
