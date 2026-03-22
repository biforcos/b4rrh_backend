package com.b4rrhh.rulesystem.catalogoption.infrastructure.web.dto;

import java.time.LocalDate;

public record DirectCatalogOptionResponse(
        String code,
        String name,
        boolean active,
        LocalDate startDate,
        LocalDate endDate
) {
}
