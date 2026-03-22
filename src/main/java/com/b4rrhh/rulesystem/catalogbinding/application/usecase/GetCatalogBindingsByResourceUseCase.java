package com.b4rrhh.rulesystem.catalogbinding.application.usecase;

import com.b4rrhh.rulesystem.catalogbinding.application.query.GetCatalogBindingsByResourceQuery;
import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogFieldBinding;

import java.util.List;

public interface GetCatalogBindingsByResourceUseCase {

    List<CatalogFieldBinding> getByResourceCode(GetCatalogBindingsByResourceQuery query);
}
