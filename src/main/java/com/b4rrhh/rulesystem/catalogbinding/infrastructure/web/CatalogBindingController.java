package com.b4rrhh.rulesystem.catalogbinding.infrastructure.web;

import com.b4rrhh.rulesystem.catalogbinding.application.query.GetCatalogBindingsByResourceQuery;
import com.b4rrhh.rulesystem.catalogbinding.application.usecase.GetCatalogBindingsByResourceUseCase;
import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogFieldBinding;
import com.b4rrhh.rulesystem.catalogbinding.infrastructure.web.dto.CatalogBindingsByResourceResponse;
import com.b4rrhh.rulesystem.catalogbinding.infrastructure.web.dto.CatalogFieldBindingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog-bindings")
public class CatalogBindingController {

    private final GetCatalogBindingsByResourceUseCase getCatalogBindingsByResourceUseCase;

    public CatalogBindingController(GetCatalogBindingsByResourceUseCase getCatalogBindingsByResourceUseCase) {
        this.getCatalogBindingsByResourceUseCase = getCatalogBindingsByResourceUseCase;
    }

    @GetMapping("/{resourceCode}")
    public ResponseEntity<CatalogBindingsByResourceResponse> getByResourceCode(@PathVariable String resourceCode) {
        List<CatalogFieldBindingResponse> fields = getCatalogBindingsByResourceUseCase
                .getByResourceCode(new GetCatalogBindingsByResourceQuery(resourceCode))
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(new CatalogBindingsByResourceResponse(resourceCode, fields));
    }

    private CatalogFieldBindingResponse toResponse(CatalogFieldBinding binding) {
        return new CatalogFieldBindingResponse(
                binding.fieldCode(),
                binding.catalogKind().name(),
                binding.ruleEntityTypeCode(),
                binding.dependsOnFieldCode(),
                binding.customResolverCode(),
                binding.active()
        );
    }
}
