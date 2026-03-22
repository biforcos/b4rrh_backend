package com.b4rrhh.rulesystem.catalogoption.domain.model;

import java.time.LocalDate;

public record DirectCatalogOption(
        String code,
        String name,
        boolean active,
        LocalDate startDate,
        LocalDate endDate
) {

    public DirectCatalogOption {
        code = normalizeRequired("code", code);
        name = normalizeRequired("name", name);
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
    }

    public boolean isEffectiveOn(LocalDate date) {
        return !startDate.isAfter(date)
                && (endDate == null || !endDate.isBefore(date));
    }

    public DirectCatalogOption withActive(boolean newActive) {
        return new DirectCatalogOption(code, name, newActive, startDate, endDate);
    }

    private static String normalizeRequired(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim();
    }
}
