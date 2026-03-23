package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record HireEmployeeResponse(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        String status,
        LocalDate hireDate,
        HiredPresenceResponse initialPresence,
        HiredLaborClassificationResponse initialLaborClassification,
        HiredContractResponse initialContract,
        HiredWorkCenterResponse initialWorkCenter
) {
}
