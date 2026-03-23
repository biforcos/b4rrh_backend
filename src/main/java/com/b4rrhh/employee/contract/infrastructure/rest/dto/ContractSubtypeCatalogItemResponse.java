package com.b4rrhh.employee.contract.infrastructure.rest.dto;

import java.time.LocalDate;

public record ContractSubtypeCatalogItemResponse(
        String code,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}