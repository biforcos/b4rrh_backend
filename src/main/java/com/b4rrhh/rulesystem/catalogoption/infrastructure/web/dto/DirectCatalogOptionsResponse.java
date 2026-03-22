package com.b4rrhh.rulesystem.catalogoption.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.List;

public record DirectCatalogOptionsResponse(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        LocalDate referenceDate,
        List<DirectCatalogOptionResponse> items
) {
}
