package com.b4rrhh.employee.contract.application.model;

import java.time.LocalDate;

public record ContractSubtypeCatalogItem(
        String code,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}