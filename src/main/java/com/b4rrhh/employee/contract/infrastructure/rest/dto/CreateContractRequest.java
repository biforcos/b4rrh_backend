package com.b4rrhh.employee.contract.infrastructure.rest.dto;

import java.time.LocalDate;

public record CreateContractRequest(
        String contractCode,
        String contractSubtypeCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
