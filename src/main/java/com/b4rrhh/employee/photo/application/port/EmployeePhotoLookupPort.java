package com.b4rrhh.employee.photo.application.port;

import java.util.Optional;

public interface EmployeePhotoLookupPort {

    Optional<EmployeePhotoContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
