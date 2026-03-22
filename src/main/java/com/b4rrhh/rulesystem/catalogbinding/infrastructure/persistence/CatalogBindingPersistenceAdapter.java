package com.b4rrhh.rulesystem.catalogbinding.infrastructure.persistence;

import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogFieldBinding;
import com.b4rrhh.rulesystem.catalogbinding.domain.port.CatalogBindingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogBindingPersistenceAdapter implements CatalogBindingRepository {

    private final SpringDataCatalogBindingRepository springDataCatalogBindingRepository;

    public CatalogBindingPersistenceAdapter(SpringDataCatalogBindingRepository springDataCatalogBindingRepository) {
        this.springDataCatalogBindingRepository = springDataCatalogBindingRepository;
    }

    @Override
    public List<CatalogFieldBinding> findActiveByResourceCode(String resourceCode) {
        return springDataCatalogBindingRepository
                .findByResourceCodeAndActiveTrueOrderByFieldCodeAsc(resourceCode)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CatalogFieldBinding toDomain(CatalogBindingEntity entity) {
        return new CatalogFieldBinding(
                entity.getResourceCode(),
                entity.getFieldCode(),
                entity.getCatalogKind(),
                entity.getRuleEntityTypeCode(),
                entity.getDependsOnFieldCode(),
                entity.getCustomResolverCode(),
                entity.isActive()
        );
    }
}
