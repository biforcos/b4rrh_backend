package com.b4rrhh.employee.workcenter.application.port;

import java.util.Optional;

public interface WorkCenterCatalogReadPort {

    Optional<String> findWorkCenterName(String ruleSystemCode, String workCenterCode);
}
