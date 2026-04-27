package com.b4rrhh.employee.labor_classification.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UpdateLaborClassificationRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        String agreementCode,
        String agreementCategoryCode
) {
}
