package com.b4rrhh.rulesystem.catalogoption.application.usecase;

import com.b4rrhh.rulesystem.catalogoption.application.query.ListWorkCentersByCompanyQuery;

public interface ListWorkCentersByCompanyUseCase {

    WorkCentersByCompanyResult get(ListWorkCentersByCompanyQuery query);
}