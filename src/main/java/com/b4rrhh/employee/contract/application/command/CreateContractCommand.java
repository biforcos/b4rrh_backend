package com.b4rrhh.employee.contract.application.command;

import java.time.LocalDate;

public record CreateContractCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String contractCode,
        String contractSubtypeCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
