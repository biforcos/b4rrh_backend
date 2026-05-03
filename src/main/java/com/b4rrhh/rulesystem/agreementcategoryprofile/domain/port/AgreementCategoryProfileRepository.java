package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;

import java.util.Optional;

public interface AgreementCategoryProfileRepository {
    Optional<AgreementCategoryProfile> findByCategoryRuleEntityId(Long categoryRuleEntityId);
    AgreementCategoryProfile save(Long categoryRuleEntityId, AgreementCategoryProfile profile);
}
