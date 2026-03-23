package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record HiredLaborClassificationResponse(
        String agreementCode,
        String agreementCategoryCode,
        LocalDate startDate
) {
}
