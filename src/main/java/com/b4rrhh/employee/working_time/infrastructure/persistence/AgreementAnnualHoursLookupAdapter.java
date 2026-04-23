package com.b4rrhh.employee.working_time.infrastructure.persistence;

import com.b4rrhh.employee.working_time.application.port.AgreementAnnualHoursLookupPort;
import com.b4rrhh.rulesystem.agreementprofile.application.port.AgreementCatalogLookupPort;
import com.b4rrhh.rulesystem.agreementprofile.infrastructure.persistence.SpringDataAgreementProfileRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Resolves annual hours from agreement_profile given an agreement business key.
 * Uses JPA-based lookups via existing repositories:
 * - AgreementCatalogLookupPort to resolve the rule entity ID from business key
 * - SpringDataAgreementProfileRepository to fetch the profile by rule entity ID
 *
 * No native SQL is needed here. The employee/date context resolution is separate
 * and handled by EmployeeAgreementContextLookupAdapter.
 */
@Component
public class AgreementAnnualHoursLookupAdapter implements AgreementAnnualHoursLookupPort {

    private final AgreementCatalogLookupPort agreementCatalogLookupPort;
    private final SpringDataAgreementProfileRepository agreementProfileRepository;

    public AgreementAnnualHoursLookupAdapter(
            AgreementCatalogLookupPort agreementCatalogLookupPort,
            SpringDataAgreementProfileRepository agreementProfileRepository
    ) {
        this.agreementCatalogLookupPort = agreementCatalogLookupPort;
        this.agreementProfileRepository = agreementProfileRepository;
    }

    @Override
    public BigDecimal resolveAnnualHours(String ruleSystemCode, String agreementCode) {
        Long ruleEntityId = agreementCatalogLookupPort
                .findAgreementRuleEntityId(ruleSystemCode, agreementCode)
                .orElseThrow(() -> new IllegalStateException(
                        "Agreement not found: " + ruleSystemCode + "/" + agreementCode
                ));

        return agreementProfileRepository
                .findByAgreementRuleEntityId(ruleEntityId)
                .map(entity -> entity.getAnnualHours())
                .orElseThrow(() -> new IllegalStateException(
                        "Agreement profile not found for " + ruleSystemCode + "/" + agreementCode
                        + ". Annual hours must be configured in agreement_profile."
                ));
    }
}
