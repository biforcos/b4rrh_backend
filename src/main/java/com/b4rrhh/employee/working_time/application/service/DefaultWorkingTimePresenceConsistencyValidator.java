package com.b4rrhh.employee.working_time.application.service;

import com.b4rrhh.employee.working_time.application.port.WorkingTimePresenceConsistencyPort;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOutsidePresencePeriodException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DefaultWorkingTimePresenceConsistencyValidator implements WorkingTimePresenceConsistencyValidator {

    private final WorkingTimePresenceConsistencyPort workingTimePresenceConsistencyPort;

    public DefaultWorkingTimePresenceConsistencyValidator(
            WorkingTimePresenceConsistencyPort workingTimePresenceConsistencyPort
    ) {
        this.workingTimePresenceConsistencyPort = workingTimePresenceConsistencyPort;
    }

    @Override
    public void validatePeriodWithinPresence(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        if (!workingTimePresenceConsistencyPort.existsPresenceContainingPeriod(employeeId, startDate, endDate)) {
            throw new WorkingTimeOutsidePresencePeriodException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    startDate,
                    endDate
            );
        }
    }
}