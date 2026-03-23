package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record HireEmployeeRequest(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        LocalDate hireDate,
        HirePresenceRequest presence,
        HireLaborClassificationRequest laborClassification,
        HireContractRequest contract,
        HireWorkCenterRequest workCenter
) {
}
