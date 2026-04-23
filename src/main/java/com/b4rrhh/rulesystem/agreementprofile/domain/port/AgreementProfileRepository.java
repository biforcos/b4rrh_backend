package com.b4rrhh.rulesystem.agreementprofile.domain.port;

import com.b4rrhh.rulesystem.agreementprofile.domain.model.AgreementProfile;

import java.util.Optional;

public interface AgreementProfileRepository {

    /**
     * Find agreement profile by agreement rule entity ID.
     *
     * @param agreementRuleEntityId the internal numeric ID of the agreement rule entity
     * @return the agreement profile if found
     */
    Optional<AgreementProfile> findByAgreementRuleEntityId(Long agreementRuleEntityId);

    /**
     * Save or update an agreement profile.
     *
     * @param agreementRuleEntityId the internal numeric ID of the agreement rule entity
     * @param profile the profile to save
     * @return the saved profile
     */
    AgreementProfile save(Long agreementRuleEntityId, AgreementProfile profile);
}
