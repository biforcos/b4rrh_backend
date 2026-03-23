package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record HiredContractResponse(
        String contractTypeCode,
        String contractSubtypeCode,
        LocalDate startDate
) {
}
