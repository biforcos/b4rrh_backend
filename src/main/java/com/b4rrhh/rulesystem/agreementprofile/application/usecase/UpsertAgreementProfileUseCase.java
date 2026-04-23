package com.b4rrhh.rulesystem.agreementprofile.application.usecase;

public interface UpsertAgreementProfileUseCase {

    /**
     * Create or update an agreement profile.
     *
     * @param command the command with profile data and business key
     * @return the saved profile result
     * @throws IllegalArgumentException if agreement not found or validation fails
     */
    AgreementProfileResult upsert(UpsertAgreementProfileCommand command);
}
