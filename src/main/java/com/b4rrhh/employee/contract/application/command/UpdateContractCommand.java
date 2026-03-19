package com.b4rrhh.employee.contract.application.command;

import java.time.LocalDate;

public record UpdateContractCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate,
        String contractCode,
        String contractSubtypeCode
) {
}
