package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;

import java.util.List;

public interface ListEmployeeWorkCentersUseCase {

    List<WorkCenter> listByEmployeeBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}