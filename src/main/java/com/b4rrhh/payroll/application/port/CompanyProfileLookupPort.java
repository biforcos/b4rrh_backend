package com.b4rrhh.payroll.application.port;

import java.util.Optional;

public interface CompanyProfileLookupPort {
    Optional<CompanyProfileContext> findByRuleSystemAndCode(String ruleSystemCode, String companyCode);
}
