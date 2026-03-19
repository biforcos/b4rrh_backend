package com.b4rrhh.employee.contract.application.command;

import java.time.LocalDate;

public record ReplaceContractFromDateCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate effectiveDate,
        String contractCode,
        String contractSubtypeCode
) {
}
