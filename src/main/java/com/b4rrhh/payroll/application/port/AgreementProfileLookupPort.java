package com.b4rrhh.payroll.application.port;

import java.util.Optional;

public interface AgreementProfileLookupPort {
    Optional<AgreementProfileContext> findByRuleSystemAndCode(String ruleSystemCode, String agreementCode);
}
