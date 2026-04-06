package com.b4rrhh.rulesystem.company.application.usecase;

import com.b4rrhh.rulesystem.company.domain.model.Company;

public interface GetCompanyUseCase {
    Company get(GetCompanyQuery query);
}
