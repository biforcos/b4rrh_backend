package com.b4rrhh.employee.lifecycle.application.command;

import java.time.LocalDate;

public record HireEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        LocalDate hireDate,
        String companyCode,
        String entryReasonCode,
        String agreementCode,
        String agreementCategoryCode,
        String contractTypeCode,
        String contractSubtypeCode,
        String workCenterCode
) {
}
