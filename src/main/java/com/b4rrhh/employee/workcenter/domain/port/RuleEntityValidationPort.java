package com.b4rrhh.employee.workcenter.domain.port;

import java.time.LocalDate;

public interface RuleEntityValidationPort {

    boolean existsActiveWorkCenterCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate);
}
