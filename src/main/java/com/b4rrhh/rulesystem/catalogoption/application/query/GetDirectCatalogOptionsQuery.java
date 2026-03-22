package com.b4rrhh.rulesystem.catalogoption.application.query;

import java.time.LocalDate;

public record GetDirectCatalogOptionsQuery(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        LocalDate referenceDate,
        String q
) {
}
