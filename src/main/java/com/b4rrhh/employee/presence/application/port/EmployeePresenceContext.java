package com.b4rrhh.employee.presence.application.port;

public record EmployeePresenceContext(
        Long employeeId,
        String ruleSystemCode
) {
}
