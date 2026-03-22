package com.b4rrhh.employee.labor_classification.application.command;

import java.time.LocalDate;

public record ListAgreementCategoryCatalogCommand(
        String ruleSystemCode,
        String agreementCode,
        LocalDate referenceDate
) {
}
