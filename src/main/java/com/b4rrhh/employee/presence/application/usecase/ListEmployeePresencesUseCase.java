package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.domain.model.Presence;

import java.util.List;

public interface ListEmployeePresencesUseCase {

    List<Presence> listByEmployeeBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
