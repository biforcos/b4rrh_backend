package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record HiredPresenceResponse(
        Integer presenceNumber,
        String companyCode,
        String entryReasonCode,
        LocalDate startDate
) {
}
