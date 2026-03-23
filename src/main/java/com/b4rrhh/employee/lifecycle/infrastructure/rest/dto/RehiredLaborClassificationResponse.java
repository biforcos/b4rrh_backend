package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record RehiredLaborClassificationResponse(
        String agreementCode,
        String agreementCategoryCode,
        LocalDate startDate
) {
}
