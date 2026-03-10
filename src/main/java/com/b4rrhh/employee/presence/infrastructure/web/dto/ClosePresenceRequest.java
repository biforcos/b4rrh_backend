package com.b4rrhh.employee.presence.infrastructure.web.dto;

import java.time.LocalDate;

public record ClosePresenceRequest(
        LocalDate endDate,
        String exitReasonCode
) {
}
