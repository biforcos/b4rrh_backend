package com.b4rrhh.employee.contact.application.port;

import java.util.Optional;

public interface ContactCatalogReadPort {

    Optional<String> findContactTypeName(String ruleSystemCode, String contactTypeCode);
}
