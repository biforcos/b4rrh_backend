package com.b4rrhh.employee.labor_classification.infrastructure.rest.dto;

public record UpdateLaborClassificationRequest(
        String agreementCode,
        String agreementCategoryCode
) {
}
