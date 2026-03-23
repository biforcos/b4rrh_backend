package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record TerminateEmployeeResponse(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate terminationDate,
        String exitReasonCode,
        String status,
        ClosedPresenceResponse closedPresence,
        ClosedContractResponse closedContract,
        ClosedLaborClassificationResponse closedLaborClassification,
        ClosedWorkCenterResponse closedWorkCenter
) {
}
