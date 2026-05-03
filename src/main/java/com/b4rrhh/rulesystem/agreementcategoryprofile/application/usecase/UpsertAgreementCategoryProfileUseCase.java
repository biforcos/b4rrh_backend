package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;

public interface UpsertAgreementCategoryProfileUseCase {
    AgreementCategoryProfile upsert(UpsertAgreementCategoryProfileCommand command);
}
