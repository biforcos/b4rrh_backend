package com.b4rrhh.rulesystem.companyprofile.application.usecase;

import com.b4rrhh.rulesystem.companyprofile.domain.model.CompanyProfile;

public interface GetCompanyProfileUseCase {

    CompanyProfile get(GetCompanyProfileQuery query);
}