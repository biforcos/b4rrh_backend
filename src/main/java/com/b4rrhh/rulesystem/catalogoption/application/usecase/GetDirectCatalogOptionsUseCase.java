package com.b4rrhh.rulesystem.catalogoption.application.usecase;

import com.b4rrhh.rulesystem.catalogoption.application.query.GetDirectCatalogOptionsQuery;

public interface GetDirectCatalogOptionsUseCase {

    DirectCatalogOptionsResult get(GetDirectCatalogOptionsQuery query);
}
