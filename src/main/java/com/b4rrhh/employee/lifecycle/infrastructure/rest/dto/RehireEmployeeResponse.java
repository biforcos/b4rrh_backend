package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record RehireEmployeeResponse(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate rehireDate,
        String status,
        RehiredPresenceResponse newPresence,
        RehiredContractResponse newContract,
        RehiredLaborClassificationResponse newLaborClassification,
        RehiredWorkCenterResponse newWorkCenter
) {
}
