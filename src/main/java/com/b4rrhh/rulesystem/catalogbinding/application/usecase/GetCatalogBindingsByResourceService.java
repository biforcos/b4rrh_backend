package com.b4rrhh.rulesystem.catalogbinding.application.usecase;

import com.b4rrhh.rulesystem.catalogbinding.application.query.GetCatalogBindingsByResourceQuery;
import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogFieldBinding;
import com.b4rrhh.rulesystem.catalogbinding.domain.port.CatalogBindingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetCatalogBindingsByResourceService implements GetCatalogBindingsByResourceUseCase {

    private final CatalogBindingRepository catalogBindingRepository;

    public GetCatalogBindingsByResourceService(CatalogBindingRepository catalogBindingRepository) {
        this.catalogBindingRepository = catalogBindingRepository;
    }

    @Override
    public List<CatalogFieldBinding> getByResourceCode(GetCatalogBindingsByResourceQuery query) {
        String normalizedResourceCode = normalizeRequiredResourceCode(query.resourceCode());
        return catalogBindingRepository.findActiveByResourceCode(normalizedResourceCode);
    }

    private String normalizeRequiredResourceCode(String resourceCode) {
        if (resourceCode == null || resourceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("resourceCode is required");
        }

        return resourceCode.trim();
    }
}
