package com.b4rrhh.employee.address.application.port;

import java.util.Optional;

public interface AddressCatalogReadPort {

    Optional<String> findAddressTypeName(String ruleSystemCode, String addressTypeCode);
}
