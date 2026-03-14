package com.b4rrhh.employee.workcenter.application.service;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;

import java.time.LocalDate;
import java.util.List;

public interface WorkCenterPresenceConsistencyValidator {

    void validatePeriodWithinPresence(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    void validatePresenceCoverageIfRequired(
            Long employeeId,
            List<WorkCenter> projectedWorkCenterHistory,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}