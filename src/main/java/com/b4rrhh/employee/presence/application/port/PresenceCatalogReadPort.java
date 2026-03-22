package com.b4rrhh.employee.presence.application.port;

import java.util.Optional;

public interface PresenceCatalogReadPort {

    Optional<String> findCompanyName(String ruleSystemCode, String companyCode);

    Optional<String> findEntryReasonName(String ruleSystemCode, String entryReasonCode);

    Optional<String> findExitReasonName(String ruleSystemCode, String exitReasonCode);
}
