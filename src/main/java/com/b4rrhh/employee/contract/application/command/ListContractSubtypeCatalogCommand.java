package com.b4rrhh.employee.contract.application.command;

import java.time.LocalDate;

public record ListContractSubtypeCatalogCommand(
        String ruleSystemCode,
        String contractTypeCode,
        LocalDate referenceDate
) {
}