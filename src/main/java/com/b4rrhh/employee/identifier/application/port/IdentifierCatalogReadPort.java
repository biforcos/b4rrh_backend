package com.b4rrhh.employee.identifier.application.port;

import java.util.Optional;

public interface IdentifierCatalogReadPort {

    Optional<String> findIdentifierTypeName(String ruleSystemCode, String identifierTypeCode);
}
