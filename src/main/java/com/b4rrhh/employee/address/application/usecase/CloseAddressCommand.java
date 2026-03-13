package com.b4rrhh.employee.address.application.usecase;

import java.time.LocalDate;

public record CloseAddressCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer addressNumber,
        LocalDate endDate
) {
}
