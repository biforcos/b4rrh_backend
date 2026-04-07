package com.b4rrhh.employee.working_time.application.service;

import java.time.LocalDate;

public interface WorkingTimePresenceConsistencyValidator {

    void validatePeriodWithinPresence(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}