package com.b4rrhh.employee.contract.application.command;

import java.time.LocalDate;

public record GetContractByBusinessKeyCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate
) {
}
