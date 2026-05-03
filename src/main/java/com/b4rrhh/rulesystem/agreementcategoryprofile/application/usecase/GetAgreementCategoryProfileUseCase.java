package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;

public interface GetAgreementCategoryProfileUseCase {
    AgreementCategoryProfile get(GetAgreementCategoryProfileQuery query);
}
