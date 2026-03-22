package com.b4rrhh.employee.labor_classification.application.model;

import java.time.LocalDate;

public record AgreementCategoryCatalogItem(
        String code,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}
