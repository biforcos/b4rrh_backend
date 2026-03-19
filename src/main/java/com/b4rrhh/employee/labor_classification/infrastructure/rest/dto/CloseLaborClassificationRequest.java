package com.b4rrhh.employee.labor_classification.infrastructure.rest.dto;

import java.time.LocalDate;

public record CloseLaborClassificationRequest(
        LocalDate endDate
) {
}
