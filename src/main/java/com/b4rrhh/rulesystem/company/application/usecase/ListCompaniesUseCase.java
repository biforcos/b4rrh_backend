package com.b4rrhh.rulesystem.company.application.usecase;

import com.b4rrhh.rulesystem.company.domain.model.Company;

import java.util.List;

public interface ListCompaniesUseCase {
    List<Company> list(ListCompaniesQuery query);
}
