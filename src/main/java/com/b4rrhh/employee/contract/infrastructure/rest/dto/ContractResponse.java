package com.b4rrhh.employee.contract.infrastructure.rest.dto;

import java.time.LocalDate;

public record ContractResponse(
        String contractCode,
        String contractTypeName,
        String contractSubtypeCode,
        String contractSubtypeName,
        LocalDate startDate,
        LocalDate endDate
) {
}
