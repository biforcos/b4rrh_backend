package com.b4rrhh.employee.labor_classification.infrastructure.rest.dto;

import java.time.LocalDate;

public record AgreementCategoryCatalogItemResponse(
        String code,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}
