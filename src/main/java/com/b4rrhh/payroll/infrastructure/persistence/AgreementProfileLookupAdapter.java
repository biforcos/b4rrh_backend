package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.application.port.AgreementProfileContext;
import com.b4rrhh.payroll.application.port.AgreementProfileLookupPort;
import com.b4rrhh.rulesystem.agreementprofile.application.port.AgreementCatalogLookupPort;
import com.b4rrhh.rulesystem.agreementprofile.infrastructure.persistence.SpringDataAgreementProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AgreementProfileLookupAdapter implements AgreementProfileLookupPort {

    private final AgreementCatalogLookupPort agreementCatalogLookupPort;
    private final SpringDataAgreementProfileRepository agreementProfileRepository;

    public AgreementProfileLookupAdapter(
            AgreementCatalogLookupPort agreementCatalogLookupPort,
            SpringDataAgreementProfileRepository agreementProfileRepository
    ) {
        this.agreementCatalogLookupPort = agreementCatalogLookupPort;
        this.agreementProfileRepository = agreementProfileRepository;
    }

    @Override
    public Optional<AgreementProfileContext> findByRuleSystemAndCode(String ruleSystemCode, String agreementCode) {
        return agreementCatalogLookupPort
                .findAgreementRuleEntityId(ruleSystemCode, agreementCode)
                .flatMap(agreementProfileRepository::findByAgreementRuleEntityId)
                .map(ap -> new AgreementProfileContext(
                        ap.getOfficialAgreementNumber(),
                        ap.getDisplayName(),
                        ap.getShortName(),
                        ap.getAnnualHours()
                ));
    }
}
