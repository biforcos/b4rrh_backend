package com.b4rrhh.employee.presence.application.port;

import java.util.Optional;

public interface EmployeePresenceLookupPort {

    Optional<EmployeePresenceContext> findById(Long employeeId);

    Optional<EmployeePresenceContext> findByIdForUpdate(Long employeeId);

    Optional<EmployeePresenceContext> findByBusinessKey(String ruleSystemCode, String employeeNumber);
}
