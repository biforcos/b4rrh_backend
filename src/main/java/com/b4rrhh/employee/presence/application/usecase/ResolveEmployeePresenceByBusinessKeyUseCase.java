package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;

public interface ResolveEmployeePresenceByBusinessKeyUseCase {

    EmployeePresenceContext resolve(String ruleSystemCode, String employeeNumber);
}
