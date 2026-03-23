package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

public record HirePresenceRequest(
        String companyCode,
        String entryReasonCode
) {
}
