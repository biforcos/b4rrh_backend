package com.b4rrhh.rulesystem.catalogbinding.domain.port;

import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogFieldBinding;

import java.util.List;

public interface CatalogBindingRepository {

    List<CatalogFieldBinding> findActiveByResourceCode(String resourceCode);
}
