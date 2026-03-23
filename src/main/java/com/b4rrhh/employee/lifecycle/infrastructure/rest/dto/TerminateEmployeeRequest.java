package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record TerminateEmployeeRequest(
        LocalDate terminationDate,
        String exitReasonCode
) {
}
