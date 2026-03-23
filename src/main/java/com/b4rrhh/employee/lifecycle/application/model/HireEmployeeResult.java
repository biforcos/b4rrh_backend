package com.b4rrhh.employee.lifecycle.application.model;

import java.time.LocalDate;

public record HireEmployeeResult(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        String status,
        LocalDate hireDate,
        Integer presenceNumber,
        String companyCode,
        String entryReasonCode,
        String agreementCode,
        String agreementCategoryCode,
        String contractTypeCode,
        String contractSubtypeCode,
        Integer workCenterAssignmentNumber,
        String workCenterCode
) {
}
