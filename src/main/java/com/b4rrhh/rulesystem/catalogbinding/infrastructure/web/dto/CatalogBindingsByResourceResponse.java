package com.b4rrhh.rulesystem.catalogbinding.infrastructure.web.dto;

import java.util.List;

public record CatalogBindingsByResourceResponse(
        String resourceCode,
        List<CatalogFieldBindingResponse> fields
) {
}
