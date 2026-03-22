package com.b4rrhh.rulesystem.catalogbinding.infrastructure.web.dto;

public record CatalogFieldBindingResponse(
        String fieldCode,
        String catalogKind,
        String ruleEntityTypeCode,
        String dependsOnFieldCode,
        String customResolverCode,
        boolean active
) {
}
