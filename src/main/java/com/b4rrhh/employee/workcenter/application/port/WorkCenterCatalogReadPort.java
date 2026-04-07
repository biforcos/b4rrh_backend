package com.b4rrhh.employee.workcenter.application.port;

import java.time.LocalDate;
import java.util.Optional;

public interface WorkCenterCatalogReadPort {

    Optional<String> findWorkCenterName(String ruleSystemCode, String workCenterCode);

    Optional<String> findWorkCenterCompanyCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate);

    Optional<String> findCompanyName(String ruleSystemCode, String companyCode);
}
