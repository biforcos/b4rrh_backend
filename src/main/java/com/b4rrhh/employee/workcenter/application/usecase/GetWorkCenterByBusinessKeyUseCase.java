package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;

import java.util.Optional;

public interface GetWorkCenterByBusinessKeyUseCase {

    Optional<WorkCenter> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer workCenterAssignmentNumber
    );
}