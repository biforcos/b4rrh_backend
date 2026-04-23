package com.b4rrhh.rulesystem.agreementprofile.application.usecase;

import java.util.Optional;

public interface GetAgreementProfileUseCase {

    /**
     * Retrieve agreement profile by business key.
     *
     * @param query the query with ruleSystemCode and agreementCode
     * @return the agreement profile if found
     */
    Optional<AgreementProfileResult> get(GetAgreementProfileQuery query);
}
