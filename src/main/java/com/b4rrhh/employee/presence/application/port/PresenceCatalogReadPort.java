package com.b4rrhh.employee.presence.application.port;

import java.util.Optional;

public interface PresenceCatalogReadPort {

    Optional<String> findCompanyName(String ruleSystemCode, String companyCode);
}
