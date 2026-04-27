package com.b4rrhh.employee.contract.application.command;

import java.time.LocalDate;

public record UpdateContractCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate,
        LocalDate newStartDate,
        String contractCode,
        String contractSubtypeCode
) {
}
