package com.b4rrhh.employee.contract.infrastructure.rest.dto;

import java.time.LocalDate;

public record UpdateContractRequest(
        LocalDate startDate,
        String contractCode,
        String contractSubtypeCode
) {
}
