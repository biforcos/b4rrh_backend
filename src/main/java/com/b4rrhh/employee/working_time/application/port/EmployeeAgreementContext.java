package com.b4rrhh.employee.working_time.application.port;

/**
 * Minimal agreement context resolved from an employee's labor classification at a given date.
 * Contains only the business keys needed to look up the agreement profile.
 */
public record EmployeeAgreementContext(String ruleSystemCode, String agreementCode) {
}
