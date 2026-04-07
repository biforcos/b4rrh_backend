package com.b4rrhh.employee.workcenter.domain.port;

import java.time.LocalDate;
import java.util.Optional;

public interface WorkCenterCompanyLookupPort {

    Optional<String> findCompanyCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate);
}