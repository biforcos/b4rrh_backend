package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record ClosedContractResponse(
        String contractTypeCode,
        String contractSubtypeCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
