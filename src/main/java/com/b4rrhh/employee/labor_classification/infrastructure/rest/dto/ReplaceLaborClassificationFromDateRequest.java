package com.b4rrhh.employee.labor_classification.infrastructure.rest.dto;

import java.time.LocalDate;

public record ReplaceLaborClassificationFromDateRequest(
        LocalDate effectiveDate,
        String agreementCode,
        String agreementCategoryCode
) {
}
