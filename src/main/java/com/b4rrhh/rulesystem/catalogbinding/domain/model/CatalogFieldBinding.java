package com.b4rrhh.rulesystem.catalogbinding.domain.model;

public record CatalogFieldBinding(
        String resourceCode,
        String fieldCode,
        CatalogKind catalogKind,
        String ruleEntityTypeCode,
        String dependsOnFieldCode,
        String customResolverCode,
        boolean active
) {

    public CatalogFieldBinding {
        resourceCode = normalizeRequired("resourceCode", resourceCode);
        fieldCode = normalizeRequired("fieldCode", fieldCode);

        if (catalogKind == null) {
            throw new IllegalArgumentException("catalogKind is required");
        }

        switch (catalogKind) {
            case DIRECT -> {
                ruleEntityTypeCode = normalizeRequired("ruleEntityTypeCode", ruleEntityTypeCode);
                dependsOnFieldCode = normalizeNullable(dependsOnFieldCode);
                customResolverCode = normalizeNullable(customResolverCode);
                if (dependsOnFieldCode != null) {
                    throw new IllegalArgumentException("dependsOnFieldCode must be null for DIRECT");
                }
                if (customResolverCode != null) {
                    throw new IllegalArgumentException("customResolverCode must be null for DIRECT");
                }
            }
            case DEPENDENT -> {
                ruleEntityTypeCode = normalizeRequired("ruleEntityTypeCode", ruleEntityTypeCode);
                dependsOnFieldCode = normalizeRequired("dependsOnFieldCode", dependsOnFieldCode);
                customResolverCode = normalizeNullable(customResolverCode);
                if (customResolverCode != null) {
                    throw new IllegalArgumentException("customResolverCode must be null for DEPENDENT");
                }
            }
            case CUSTOM -> {
                ruleEntityTypeCode = normalizeNullable(ruleEntityTypeCode);
                dependsOnFieldCode = normalizeNullable(dependsOnFieldCode);
                customResolverCode = normalizeRequired("customResolverCode", customResolverCode);
                if (ruleEntityTypeCode != null) {
                    throw new IllegalArgumentException("ruleEntityTypeCode must be null for CUSTOM");
                }
                if (dependsOnFieldCode != null) {
                    throw new IllegalArgumentException("dependsOnFieldCode must be null for CUSTOM");
                }
            }
            default -> throw new IllegalStateException("Unsupported catalogKind: " + catalogKind);
        }
    }

    private static String normalizeRequired(String fieldName, String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return normalized;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
