package com.b4rrhh.rulesystem.companyprofile.application.usecase;

import com.b4rrhh.rulesystem.companyprofile.domain.model.CompanyProfile;

public interface UpsertCompanyProfileUseCase {

    CompanyProfile upsert(UpsertCompanyProfileCommand command);
}