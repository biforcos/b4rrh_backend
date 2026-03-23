package com.b4rrhh.employee.lifecycle.application.command;

import java.time.LocalDate;

public record RehireEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate rehireDate,
        String entryReasonCode,
        String companyCode,
        String agreementCode,
        String agreementCategoryCode,
        String contractTypeCode,
        String contractSubtypeCode,
        String workCenterCode
) {
}