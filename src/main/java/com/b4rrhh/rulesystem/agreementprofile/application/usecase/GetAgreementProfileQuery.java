package com.b4rrhh.rulesystem.agreementprofile.application.usecase;

import java.util.Optional;

public record GetAgreementProfileQuery(
        String ruleSystemCode,
        String agreementCode
) {
}
