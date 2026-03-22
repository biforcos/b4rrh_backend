package com.b4rrhh.rulesystem.catalogbinding.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCatalogBindingRepository
        extends JpaRepository<CatalogBindingEntity, CatalogBindingEntityId> {

    List<CatalogBindingEntity> findByResourceCodeAndActiveTrueOrderByFieldCodeAsc(String resourceCode);
}
