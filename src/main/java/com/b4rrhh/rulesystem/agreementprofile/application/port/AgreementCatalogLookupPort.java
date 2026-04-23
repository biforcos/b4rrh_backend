package com.b4rrhh.rulesystem.agreementprofile.application.port;

import java.util.Optional;

public interface AgreementCatalogLookupPort {

    /**
     * Find agreement rule entity ID by business key.
     *
     * @param ruleSystemCode the rule system code
     * @param agreementCode the agreement code
     * @return the agreement rule entity ID if found
     */
    Optional<Long> findAgreementRuleEntityId(String ruleSystemCode, String agreementCode);
}
